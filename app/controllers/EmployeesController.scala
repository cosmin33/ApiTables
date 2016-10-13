package controllers

import apitables._
import apitables.fields._

object EmployeesController extends BaseController {

  val employeesBuilder = new TableBuilder("employees e").fields(
    IntegerFB("id").tableName("e.id").primaryKey().build,
    StringFB("name").tableName("e.name").filterable().build,
    StringFB("address").tableName("e.address").nullable().build,
    IntegerFB("departmentId").tableName("e.department_id").filterable().nullable().invisible().build
  )
  val departmentsBuilder = new TableBuilder("departments d").fields(
    IntegerFB("id").tableName("d.id").primaryKey().build,
    StringFB("name").tableName("d.name").build
  )
  val paymentsBuilder = new TableBuilder("payments").fields(
    IntegerFB("id").tableName("id").primaryKey().build,
    IntegerFB("empId").tableName("emp_id").filterable().invisible().build,
    TimestampFB("date").tableName("date").build,
    DoubleFB("value").tableName("value").build,
    StringFB("description").tableName("description").nullable().build
  )

  val employeesTable = employeesBuilder.build
  val paymentsTable = paymentsBuilder.build
  val empPaymentsRelation = new RelationBuilder("payments")
    .master(employeesTable)
    .detail(paymentsTable)
    .joinConditions(("id", "empId"))
    .build
  val departmentsTable = departmentsBuilder.build
  val empDeptRelation = new RelationBuilder("department")
    .master(employeesTable)
    .detail(departmentsTable)
    .lookup()
    .joinConditions(("departmentId", "id"))
    .build

  override def mainTable: Table = employeesTable

}
