-- Function to process geometry union from GeoJSON
CREATE OR REPLACE FUNCTION public.process_geojson_union(geojson_input text)
RETURNS jsonb AS $$
DECLARE
    features jsonb;
    input_jsonb jsonb;
BEGIN
    -- Convert GeoJSON text to JSONB
    input_jsonb := geojson_input::jsonb;

    WITH input_data AS (
        SELECT
            row_number() OVER () AS gid,
            ST_SetSRID(ST_GeomFromGeoJSON(f->>'geometry'), 4326) AS geom,
            f->'properties'->>'tipo' AS tipo
        FROM jsonb_array_elements(input_jsonb->'features') f
    ),
    clusters AS (
        SELECT
            d1.gid,
            d1.tipo,
            d1.geom,
            min(d2.gid) OVER (PARTITION BY d1.tipo ORDER BY d1.gid) AS cluster_id
        FROM input_data d1
        LEFT JOIN input_data d2
            ON d1.tipo = d2.tipo
           AND ST_Intersects(d1.geom, d2.geom)
    ),
    union_clusters AS (
        SELECT
            tipo,
            cluster_id,
            ST_Union(geom) AS geom
        FROM clusters
        GROUP BY tipo, cluster_id
    ),
    cut_clusters AS (
        SELECT
            a.tipo,
            a.cluster_id,
            ST_Difference(
                a.geom,
                COALESCE(
                    (SELECT ST_Union(b.geom)
                     FROM union_clusters b
                     WHERE b.tipo <> a.tipo),
                    ST_GeomFromText('POLYGON EMPTY', 4326))
            ) AS geom,
            a.geom AS original_geom
        FROM union_clusters a
    ),
    -- Always select one geometry per cluster: if empty, fallback to original
    final_clusters AS (
        SELECT
            tipo,
            cluster_id,
            CASE
                WHEN ST_IsEmpty(geom) THEN original_geom
                ELSE geom
            END AS geom,
            ST_IsEmpty(geom) AS was_empty
        FROM cut_clusters
    ),
    features_json AS (
        SELECT jsonb_build_object(
            'type', 'Feature',
            'geometry', (ST_AsGeoJSON(geom))::jsonb,
            'properties', jsonb_build_object('tipo', tipo, 'was_empty', was_empty)
        ) AS feature
        FROM final_clusters
    )
    SELECT jsonb_build_object(
        'type', 'FeatureCollection',
        'features', jsonb_agg(feature)
    ) INTO features
    FROM features_json;

    RETURN features;
END;
$$ LANGUAGE plpgsql;

-- Function explanatory comment
COMMENT ON FUNCTION public.process_geojson_union(text) IS 'Processes a GeoJSON feature collection, grouping and uniting geometries by type, and returns a GeoJSON FeatureCollection with the resulting geometries.';

-- Permissions
GRANT EXECUTE ON FUNCTION public.process_geojson_union(text) TO postgis_calculator;

-- Rollback
-- DROP FUNCTION IF EXISTS public.process_geojson_union(text);
