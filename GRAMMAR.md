# Grammar
```
regex: concat union;

union: '|' concat union;
union: ε;

concat: kleene suffix;

suffix: kleene suffix;
suffix: ε;

kleene: base star;

star: '*';
star: ε;

base: LIT;
base: '(' regex ')';
base: '[' negation inhalt range ']';

negation: '^';
negation: ε;

range: inhalt range;
range: ε;

inhalt: LIT rest;

rest: '-' LIT;
rest: ε;

```

# First Follow Sets
| NonTerminal                          | First   | Follow           | Select         |
| :----------------------------------- | :------ | :--------------- | :------------- |
| regex: concat union;                 | LIT ( [ | $ )              | LIT ( [        |
| union: '\|' concat union;            | \|      | $ )              | \|             |
| union: ε;                            | ε       | $ )              | $ )            |
| concat: kleene suffix;               | LIT ( [ | $ ) \|           | LIT ( [        |
| suffix: kleene suffix;               | LIT ( [ | $ ) \|           | LIT ( [        |
| suffix: ε;                           | ε       | $ ) \|           | $ ) \|         |
| kleene: base star;                   | LIT ( [ | LIT ( [ $ ) \|   | LIT ( [        |
| star: '*';                           | *       | LIT ( [ $ ) \|   | *              |
| star: ε;                             | ε       | LIT ( [ $ ) \|   | LIT ( [ $ ) \| |
| base: LIT;                           | LIT     | LIT ( [ $ ) \| * | LIT            |
| base: '(' regex ')';                 | (       | LIT ( [ $ ) \| * | (              |
| base: '[' negation inhalt range ']'; | [       | LIT ( [ $ ) \| * | [              |
| negation: '^';                       | ^       | LIT              | ^              |
| negation: ε;                         | ε       | LIT              | LIT            |
| range: inhalt range;                 | LIT     | ]                | LIT            |
| range: ε;                            | ε       | ]                | ]              |
| inhalt: LIT rest;                    | LIT     | ] LIT            | LIT            |
| rest: '-' LIT;                       | -       | ] LIT            | -              |
| rest: ε;                             | ε       | ] LIT            | ] LIT          |
