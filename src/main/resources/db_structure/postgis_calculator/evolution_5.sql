-- DROP FUNCTION geometry_bases.consolidated_area(jsonb, numeric, numeric, numeric);

CREATE OR REPLACE FUNCTION geometry_bases.consolidated_area(p_fc jsonb, p_opt1 numeric DEFAULT 1, p_opt2 numeric DEFAULT 1, p_rl_min_percent numeric DEFAULT 50)
 RETURNS jsonb
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  srid_in  int := 4674;
  -- srid_met e g_agro_clean_m REMOVIDOS - Usaremos 'geography'

  g_imovel geometry;

  -- Agro (entrada/saída)
  g_agro_raw   geometry;
  g_agro_clip  geometry;
  g_agro_clean geometry;
  area_agro_ha numeric := 0;

  -- Camadas para recortes
  g_infra      geometry;
  g_pub_infra  geometry;
  g_river_poly geometry;
  g_lake_poly  geometry;
  g_spring_pt  geometry;
  g_river_line geometry;

  -- Propriedades originais a preservar
  props_orig jsonb := '{}'::jsonb;
  feat_new   jsonb;
BEGIN
  -- 1) Imóvel
  -- CORREÇÃO: f_get_layer_geom já retorna geometria válida e unificada.
  -- A sanitização (ST_Buffer) foi removida.
  g_imovel := geometry_bases.f_get_layer_geom(p_fc, 'RURAL_PROPERTY', 'POLYGON');
  IF g_imovel IS NULL OR ST_IsEmpty(g_imovel) THEN
    RETURN p_fc;
  END IF;

  -- 2) Guardar as PROPRIEDADES ORIGINAIS
  SELECT feat->'properties'
    INTO props_orig
  FROM jsonb_array_elements(p_fc->'features') AS feat
  WHERE upper(feat->'properties'->>'layerCode') = 'CONSOLIDATED_AREA'
  LIMIT 1;
  props_orig := COALESCE(props_orig, '{}'::jsonb);

  -- 3) Agro original
  g_agro_raw := geometry_bases.f_get_layer_geom(p_fc, 'CONSOLIDATED_AREA', 'POLYGON');
  IF g_agro_raw IS NULL OR ST_IsEmpty(g_agro_raw) THEN
    RETURN p_fc;
  END IF;

  -- 4) Recorte obrigatório ao imóvel
  -- CORREÇÃO: g_agro_raw já é válida, ST_Buffer(ST_MakeValid(...)) removido.
  g_agro_clip := ST_Intersection(g_agro_raw, g_imovel);
  IF g_agro_clip IS NULL OR ST_IsEmpty(g_agro_clip) THEN
    RETURN geometry_bases.f_fc_replace_layer(p_fc, 'CONSOLIDATED_AREA', NULL);
  END IF;

  g_agro_clean := g_agro_clip; -- Começa com a área consolidada já clipada

  -- 6) Demais recortes (rios, lagos, infra…)
  g_infra      := geometry_bases.f_get_layer_geom(p_fc, 'PROPERTY_INFRASTRUCTURE', 'POLYGON');
  g_pub_infra  := geometry_bases.f_get_layer_geom(p_fc, 'PUBLIC_INFRASTRUCTURE',   'POLYGON');
  g_river_poly := geometry_bases.f_get_layer_geom(p_fc, 'RIVER',                   'POLYGON');
  g_lake_poly  := geometry_bases.f_get_layer_geom(p_fc, 'LAKE_LAGOON',             'POLYGON');
  g_spring_pt  := geometry_bases.f_get_layer_geom(p_fc, 'RIVER_SPRING',            'POINT');
  g_river_line := geometry_bases.f_get_layer_geom(p_fc, 'RIVER_UP_TO_10M',         'LINESTRING');

  IF g_infra IS NOT NULL AND NOT ST_IsEmpty(g_infra) THEN
    g_agro_clean := ST_Difference(g_agro_clean, g_infra);
  END IF;
  IF g_pub_infra IS NOT NULL AND NOT ST_IsEmpty(g_pub_infra) THEN
    g_agro_clean := ST_Difference(g_agro_clean, g_pub_infra);
  END IF;
  IF g_river_poly IS NOT NULL AND NOT ST_IsEmpty(g_river_poly) THEN
    g_agro_clean := ST_Difference(g_agro_clean, g_river_poly);
  END IF;
  IF g_lake_poly IS NOT NULL AND NOT ST_IsEmpty(g_lake_poly) THEN
    g_agro_clean := ST_Difference(g_agro_clean, g_lake_poly);
  END IF;

  /* -----------------------------------------------------------------
   * CORREÇÃO: Buffers de nascente e rio refeitos usando 'geography'
   * para garantir o cálculo correto em metros.
   * (Nota: O buffer de 1m para nascente parece estranho, mas foi mantido
   * conforme o código original.)
   * -----------------------------------------------------------------
  */
  IF g_spring_pt IS NOT NULL AND NOT ST_IsEmpty(g_spring_pt) THEN
    g_agro_clean := ST_Difference(
      g_agro_clean,
      (ST_Buffer(g_spring_pt::geography, 1))::geometry -- Buffer de 1m
    );
  END IF;
  IF g_river_line IS NOT NULL AND NOT ST_IsEmpty(g_river_line) THEN
    g_agro_clean := ST_Difference(
      g_agro_clean,
      (ST_Buffer(g_river_line::geography, 1))::geometry -- Buffer de 1m
    );
  END IF;

  -- 7) Reforço de recorte + consolidação
  -- CORREÇÃO: Recorte duplo (ST_Intersection) removido por ser redundante.
  IF g_agro_clean IS NULL OR ST_IsEmpty(g_agro_clean) THEN
    RETURN geometry_bases.f_fc_replace_layer(p_fc, 'CONSOLIDATED_AREA', NULL);
  END IF;

  -- Aplica uma limpeza final. (ST_Union é redundante, mas ST_Buffer(ST_MakeValid) é bom)
  g_agro_clean   := ST_Buffer(ST_MakeValid(g_agro_clean), 0);

  -- CORREÇÃO: Cálculo de área refeito usando 'geography'
  area_agro_ha   := round( (ST_Area(g_agro_clean::geography) / 10000.0)::numeric, 4 );

  -- 8) Substitui o layer mantendo as properties originais + área (ha)
  feat_new := geometry_bases.f_make_feature(
                'CONSOLIDATED_AREA',
                g_agro_clean,
                props_orig || jsonb_build_object(
                  'area_ha', area_agro_ha,
                  'area',    area_agro_ha
                )
              );

  RETURN geometry_bases.f_fc_replace_layer(p_fc, 'CONSOLIDATED_AREA', feat_new);
END;
$function$
;
