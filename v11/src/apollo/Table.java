package apollo;
import java.lang.annotation.*;

/**
* This represents a Table in the database.  Each instance of this is a row.
*
* Each field must be public, and the name included in field order.
* Use the PrimaryKey, Key, NotNull and Bool annotations where appropriate.
*
* PrimaryKeys should not have the NotNull annotation.  The field can be null, which
* means the database will automatically assign a value.
*/
public interface Table {
	/**
	* Return an array of the field names.  The first one should be the primary key.  This is needed because
	* reflection does not guarantee a specific order.
	*/
	public String[] getFieldNames();

	/**
	* We highly recommend that the class name matches the table name.  However, there may be some differences
	* such as lower case or underscores.  The table name must not have a dot in it.
	*/
	public String getTableName();

	//===============================
	/**
	* Used to annotate that a field cannot be null.
	*/
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface NotNull {}

	/**
	* Used to annotate that a field is a PrimaryKey.  Field type must be int or long.
	*/
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface PrimaryKey {}

	/**
	* Used to annotate that a field is a boolean.  Field type must be either boolean or int.
	* It cannot be null.
	*/
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Bool {}

}