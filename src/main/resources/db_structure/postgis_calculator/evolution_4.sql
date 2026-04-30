-- DROP FUNCTION geometry_bases.native_vegetation(jsonb);

CREATE OR REPLACE FUNCTION geometry_bases.native_vegetation(p_fc jsonb)
 RETURNS jsonb
 LANGUAGE plpgsql
 STABLE
AS $function$
DECLARE
  srid_in  int := 4674;
  -- srid_met e g_nv_clean_m REMOVIDOS - Usaremos 'geography'

  -- imóvel
  g_imovel geometry;

  -- NV
  g_nv_raw     geometry;
  g_nv_clip    geometry;
  g_nv_clean   geometry;
  area_nv_ha   numeric := 0;

  -- recortes "obrigatórios"
  g_ca_poly    geometry;
  g_river_poly geometry;
  g_lake_poly  geometry;
  g_pub_infra  geometry;
  g_spring_pt  geometry;
  g_river_line geometry;

  src_layer text := NULL;
  props_orig jsonb := '{}'::jsonb;
  feat_new jsonb;
BEGIN
  -- 1) imóvel
  -- CORREÇÃO: f_get_layer_geom já retorna geometria válida e unificada.
  -- A sanitização (ST_Buffer) foi removida.
  g_imovel := geometry_bases.f_get_layer_geom(p_fc, 'RURAL_PROPERTY', 'POLYGON');
  IF g_imovel IS NULL OR ST_IsEmpty(g_imovel) THEN
    RETURN p_fc;
  END IF;

  -- 2) decidir fonte da NV e capturar suas PROPRIEDADES originais
  g_nv_raw := geometry_bases.f_get_layer_geom(p_fc, 'REMAINING_NATIVE_VEGETATION', 'POLYGON');
  IF g_nv_raw IS NOT NULL AND NOT ST_IsEmpty(g_nv_raw) THEN
    src_layer := 'REMAINING_NATIVE_VEGETATION';
  ELSE
    g_nv_raw := geometry_bases.f_get_layer_geom(p_fc, 'NATIVE_VEGETATION', 'POLYGON');
    IF g_nv_raw IS NOT NULL AND NOT ST_IsEmpty(g_nv_raw) THEN
      src_layer := 'NATIVE_VEGETATION';
    ELSE
      RETURN p_fc; -- não há NV para processar
    END IF;
  END IF;

  -- Captura propriedades originais
  SELECT feat->'properties'
    INTO props_orig
  FROM jsonb_array_elements(p_fc->'features') AS feat
  WHERE upper(feat->'properties'->>'layerCode') = src_layer
  LIMIT 1;
  props_orig := COALESCE(props_orig, '{}'::jsonb);

  -- 3) recorte obrigatório ao imóvel
  -- CORREÇÃO: g_nv_raw já é válida, ST_Buffer(ST_MakeValid(...)) removido.
  g_nv_clip := ST_Intersection(g_nv_raw, g_imovel);
  IF g_nv_clip IS NULL OR ST_IsEmpty(g_nv_clip) THEN
    RETURN geometry_bases.f_fc_replace_layer(p_fc, src_layer, NULL);
  END IF;

  -- 4) carregar geometrias para recorte
  g_ca_poly    := geometry_bases.f_get_layer_geom(p_fc, 'CONSOLIDATED_AREA', 'POLYGON');
  g_river_poly := geometry_bases.f_get_layer_geom(p_fc, 'RIVER',         'POLYGON');
  g_lake_poly  := geometry_bases.f_get_layer_geom(p_fc, 'LAKE_LAGOON',   'POLYGON');
  g_pub_infra  := geometry_bases.f_get_layer_geom(p_fc, 'PUBLIC_INFRASTRUCTURE', 'POLYGON');
  g_spring_pt  := geometry_bases.f_get_layer_geom(p_fc, 'RIVER_SPRING',  'POINT');
  g_river_line := geometry_bases.f_get_layer_geom(p_fc, 'RIVER_UP_TO_10M','LINESTRING');

  g_nv_clean := g_nv_clip; -- Começa com a NV clipada pelo imóvel

  -- 4.1) Aplicar recortes (ST_Difference)
  IF g_ca_poly IS NOT NULL AND NOT ST_IsEmpty(g_ca_poly) THEN
    g_nv_clean := ST_Difference(g_nv_clean, g_ca_poly);
  END IF;
  IF g_river_poly IS NOT NULL AND NOT ST_IsEmpty(g_river_poly) THEN
    g_nv_clean := ST_Difference(g_nv_clean, g_river_poly);
  END IF;
  IF g_lake_poly IS NOT NULL AND NOT ST_IsEmpty(g_lake_poly) THEN
    g_nv_clean := ST_Difference(g_nv_clean, g_lake_poly);
  END IF;
  IF g_pub_infra IS NOT NULL AND NOT ST_IsEmpty(g_pub_infra) THEN
    g_nv_clean := ST_Difference(g_nv_clean, g_pub_infra);
  END IF;

  /* -----------------------------------------------------------------
   * CORREÇÃO: Buffers de nascente e rio refeitos usando 'geography'
   * para garantir o cálculo correto em metros, independentemente do UTM.
   * -----------------------------------------------------------------
  */
  IF g_spring_pt IS NOT NULL AND NOT ST_IsEmpty(g_spring_pt) THEN
    g_nv_clean := ST_Difference(
      g_nv_clean,
      -- 1. Converte ponto para geography
      -- 2. Aplica buffer de 50m (em geography)
      -- 3. Converte o resultado de volta para geometry para o ST_Difference
      (ST_Buffer(g_spring_pt::geography, 50))::geometry
    );
  END IF;
  IF g_river_line IS NOT NULL AND NOT ST_IsEmpty(g_river_line) THEN
    g_nv_clean := ST_Difference(
      g_nv_clean,
      -- Mesma lógica, mas com buffer de 1m
      (ST_Buffer(g_river_line::geography, 1))::geometry
    );
  END IF;

  -- 5) saneamento final
  -- CORREÇÃO: O segundo ST_Intersection com g_imovel (linha 98) foi removido por ser redundante.
  IF g_nv_clean IS NULL OR ST_IsEmpty(g_nv_clean) THEN
    RETURN geometry_bases.f_fc_replace_layer(p_fc, src_layer, NULL);
  END IF;

  -- Garante que a geometria final seja válida e dissolvida
  g_nv_clean := ST_Buffer(ST_MakeValid(g_nv_clean), 0);

  /* -----------------------------------------------------------------
   * CORREÇÃO: Cálculo de área refeito usando 'geography'
   * -----------------------------------------------------------------
  */
  area_nv_ha := round((ST_Area(g_nv_clean::geography)/10000.0)::numeric, 4);

  -- 6) substitui o layer original, preservando properties + área
  feat_new := geometry_bases.f_make_feature(
                src_layer,      -- mantém o MESMO layerCode de entrada
                g_nv_clean,     -- geometria final limpa (em 4674)
                props_orig || jsonb_build_object(
                  'area_ha', area_nv_ha,
                  'area',    area_nv_ha
                )
              );
  -- f_make_feature irá converter g_nv_clean de 4674 para 4326 para o GeoJSON final

  RETURN geometry_bases.f_fc_replace_layer(p_fc, src_layer, feat_new);
END;
$function$
;
