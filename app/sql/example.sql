/*
drop table payments;
drop table employees;
drop table departments;
 */
create table employees(
  id integer not null,
  name varchar(50) not null,
  address varchar(120),
  department_id integer,
  constraint emp_pk primary key(id),
  constraint emp_name_uk unique(name)
);

create table departments(
  id integer not null,
  name varchar(50) not null,
constraint dept_pk primary key(id),
constraint dept_name_uk unique(name)
);

create table payments(
  id integer not null,
  emp_id integer not null,
  date timestamp without time zone not null,
  value numeric(18,2) not null,
  description varchar(120),
constraint payment_pk primary key(id)
);

alter table employees add constraint client_dept_fk foreign key(department_id) references departments(id);
alter table payments add constraint payment_client_fk foreign key(emp_id) references employees(id);

insert into departments(id, name)
  values(1, 'sales');
insert into departments(id, name)
  values(2, 'engineering');
insert into departments(id, name)
  values(3, 'software');

insert into employees(id, name, address, department_id)
  values(1, 'the dude', null, 1);
insert into employees(id, name, address, department_id)
  values(2, 'osho', 'mumbay', 3);
insert into employees(id, name, address, department_id)
  values(3, 'Tom', 'str Selari', 3);

insert into payments(id, emp_id, date, value, description)
  values(1, 1, '01/01/2014', 140, 'First payment');
insert into payments(id, emp_id, date, value, description)
  values(2, 1, '06/23/2014', 140, 'Second payment');
insert into payments(id, emp_id, date, value, description)
  values(3, 3, '08/16/2014', 140, 'Some payment');

alter table employees add constraint name_test_ck check (name <> 'check_test');

create or replace function employees_bi() returns trigger as $$
begin
  if (new.name = 'raise_test') then
    raise exception 'test exception raised' using errcode = '20100', hint = 'hint';
  end if;
  return new;
end;
$$ language 'plpgsql' security definer;

drop trigger if exists employees_bi on employees;

create trigger employees_bi before insert on employees
  for each row execute procedure employees_bi();
