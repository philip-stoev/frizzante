# 

# Dynamic Production Weights

It is possible to control the weight of each grammar production at runtime. The weight determines how often a production is chosen.

For example, consider the following grammar meant to generate SQL statements:

```
main:
	SELECT COUNT(*) FROM table_name WHERE f1 = value ;

table_name:
	T1 | T2 | T3 ;

value:
	1 | 2 | 3 ;
```

Suppose that, in a particular database configuration, table T3 does not exist at all.
Any queries targeted at that table will fail immediately, providing almost no testing benefit.
Therefore, it would be beneficial if a smaller number such queries are generated.

Furthermore, suppose that the value 3 is never found in the table so that COUNT(*) returns 0.
Such a query would be somewhat beneficial, but we may wish to stress test the code paths in the
database product by processing actual records.

So, once a query has been generated and executed, you can determine whether this query was beneficial
or not and provide a penalty factor. Any grammar productions used in generating the query will be penalized
and will thus be less likely to be picked in the future.

```
Context context = new Context.ContextBuilder(grammar).build();

while (true) {
	Sentence<String> sentence = new Sentence<String>();
	context.generate(sentence);
	String query = sentence.toString();

	try {
	ResultSet rs = stmt.executeQuery(query);
	if (rs.getInt(1) == 0){
		sentence.fail(0.1);
	} else {
		sentence.success(0.1);
	}
	} catch (SQLException e) {
		sentence.fail(0.9);
	}
}
```

`fail` takes a float argument from 0 to 1. A penalty of 1 will penalize the participating productions so
much that they will never be taken if any alternatives are available.

In the example above, the calls to `fail` and `success` are balanced so that otherwise useful grammar
productions that happen to participate in unhelpful generations will eventually be rehabilitated,
but only up to their original weight.

# Generating Code

It is possible to generate fully-formed code directly. This avoids the need to generate a string representation
first and then parse it.

In the body of a grammar rule, only two characters require special handling – the semicolon and the pipe.
To generate code that contains such characters without undue interference, load the grammar file using the
`TRAILING_PIPE` and `STANDALONE_SEMICOLON` flags. This will cause the tool to only handle pipe characters
that are at the end of the line and semicolon characters that are on a line of their own.

All other characters and instances of pipe and semicolon will be passed verbatim into the generated output.

For example, consider the following test for the insert functionality of MongoDB. The MongoDB insert call uses BasicObject objects that can be nested or chained.

```
main:
        BasicObject obj = basic_object;
        coll.insert(obj);
;

basic_object:
	new BasicObject(object_parameters).append_list
;

append_list:
	|
	append_item |
	append_list.append_item;

append_item:
	append(object_parameters)
;

object_parameters:
	name, value
;

name:
	"string"
;

value:
	"string" |
	integer |
	basic_object
;

string:
	ABC |
	XYZ
;

integer:
	1 |
	2
;
```

You can then tack on some header and footer, compile it and run it. Because compiling Java from a String requires a lot of boilerplate code,
a convenience function is provided for the purpose.

```
TODO
```

# Embedding Java Code

It is possible to embed Java code in the grammar. The code will be called each time a particular production is used. Each piece of code will called
as follows:

```
public static void generate(final Context context, final Sentence<Object> sentence) {
	...
}
```

So you have access to the `Context` object that provides access to facilities such as random number generation, and the `Sentence` object which implements
the `Appendable` interface so that you can produce output.

For example:

```
main: rule1 | rule2 ;

rule1.java: {{
	sentence.append("Triggered rule #1");
}};

rule2.java: {{
	sentence.append("Triggered rule #2");
}};
```

Each Java snippet must be in a rule on its own, with `.java` appended to the name of the rule at the point of definition.
The code needs to be enclosed in `{{` and `}};`

# Generating Object Lists

Instead of strings, it is possible to generate Lists of Java objects directly. This avoids the need to generate the output in a string form
and then parse it to convert it to objects.

The only requirement is that all grammar productions produce Java objects. It is not possible to generate a mixture of objects and strings
-- a `ClassCastException` will be thrown. No separator can be used.

Here is a code that generates some `Longs`:

```
Grammar grammar = new Grammar("main: generate_long generate_long; generate_long.java: {{ sentence.add(new Long(2)); }};");
Context context = new Context.ContextBuilder(grammar).nullSeparator().build();

Sentence<Long> sentence = new Sentence<Long>();
context.generate(sentence);
Iterator<Long> iterator = sentence.iterator();
```

You can then iterate over the objects that were generated.

