select
  case data_type
    when 'double precision' then 'DoubleFB'
    when 'character varying' then 'StringFB'
    when 'bigint' then 'IntegerFB'
    when 'smallint' then 'IntegerFB'
    when 'integer' then 'IntegerFB'
    when 'numeric' then 'DoubleFB'
    when 'timestamp without time zone' then 'TimestampFB'
    when 'boolean' then 'BooleanFB'
    else 'Unknown'
  end ||
  '("' || lower(substring(replace(initcap(replace(column_name, '_', ' ')), ' ', '') from 1 for 1))
  || substring(replace(initcap(replace(column_name, '_', ' ')), ' ', '') from 2) || '")' ||
  '.tableName("' || column_name || '")' ||
  case is_nullable when 'YES' then '.nullable()' else '' end || '.build' || ','
from information_schema.columns
where table_schema = 'public'
and table_name   = 'event'
order by ordinal_position;

/*
create function field_builder(varchar) returns table(builders varchar) as $$
select
  case data_type
  when 'double precision' then 'DoubleFB'
  when 'character varying' then 'StringFB'
  when 'bigint' then 'IntegerFB'
  when 'smallint' then 'IntegerFB'
  when 'integer' then 'IntegerFB'
  when 'numeric' then 'DoubleFB'
  when 'timestamp without time zone' then 'TimestampFB'
  when 'boolean' then 'BooleanFB'
  else 'Unknown'
  end ||
  '("' || lower(substring(replace(initcap(replace(column_name, '_', ' ')), ' ', '') from 1 for 1))
  || substring(replace(initcap(replace(column_name, '_', ' ')), ' ', '') from 2) || '")' ||
  '.tableName("' || column_name || '")' ||
  case is_nullable when 'YES' then '.nullable()' else '' end || '.build' || ','
from information_schema.columns
where table_schema = 'public'
      and table_name   = $1
order by ordinal_position
$$ language sql;
*/

/*
select column_name, is_nullable, data_type, is_identity
  from information_schema.columns
  where table_schema = 'public'
    and table_name   = 'payments'
  order by ordinal_position;
*/
