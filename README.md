# Experimental stage. do not use!

# ApiTables
ApiTables is a scala library for exposing your database with REST API. Built with Playframework

Example:

in BooksController.scala:
```
  val assetsBuilder = new TableBuilder("books b").fields(
    IntegerFB("id").tableName("b.id").primaryKey().filterable().insertReturning().build,
    TimestampFB("published").tableName("b.published").lookup().insertReturning().build,
    StringFB("name").tableName("b.name").build,
  ).joinClause("left join authors a on b.author_id = a.id")

```
and in routes:
```
GET           /books              controllers.BooksController.filter
GET           /books/row          controllers.BooksController.row
POST          /books              controllers.BooksController.insert
PUT           /books              controllers.BooksController.update
DELETE        /books              controllers.BooksController.delete
```
