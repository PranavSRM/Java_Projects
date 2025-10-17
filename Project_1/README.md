# CalculatorApp

A simple desktop calculator built with Java Swing. This small app implements a custom expression evaluator that supports binary arithmetic (+, -, *, /), decimal numbers, and parentheses. The UI provides common calculator buttons including Clear (C), backspace (←), parentheses, decimal point, and an equals button to evaluate expressions.

This README explains what the program is, how to build and run it, usage examples, known limitations, and suggestions for further improvements.

## Features

- Graphical user interface built with Java Swing
- Support for:
  - Addition, subtraction, multiplication, division
  - Parentheses for grouping
  - Decimal numbers
  - Clear (C) and Backspace (←)
- Custom expression evaluator (shunting-yard-like with two stacks)
- Uses Java 8+ language features (method references)

## Requirements

- Java Development Kit (JDK) 8 or newer
- No external dependencies (pure Java / Swing)

## Files

- `Project_1/CalculatorApp.java` — main application source (default package)

## Build & run

Two common approaches are shown below.

Option A — compile and run from the folder containing the source:

```bash
cd Project_1
javac CalculatorApp.java
java CalculatorApp
```

Option B — compile from repository root and run specifying classpath:

```bash
javac Project_1/CalculatorApp.java
java -cp Project_1 CalculatorApp
```

If you prefer packaging into a JAR:

```bash
cd Project_1
javac CalculatorApp.java
jar cfe CalculatorApp.jar CalculatorApp CalculatorApp.class
java -jar CalculatorApp.jar
```

(When creating a JAR you may want to place the class in a package first — default-package classes cannot be imported by other classes.)

## Usage

- Click digits and operators to build an expression.
- Parentheses `(` and `)` can be used to change evaluation order.
- `.` inserts a decimal point.
- `C` clears the current expression.
- `←` removes the last entered character.
- `=` evaluates the expression and displays "expression = result" in the display; the result becomes the new expression (so you can continue calculating).

Example button sequences:
- 7 × ( 3 + 2 ) = → shows `7*(3+2) = 35.0`
- 12.5 ÷ 2 = → shows `12.5/2 = 6.25`
- 5 + 4 * 3 = → respects operator precedence (multiplies before adding)

## Implementation notes

- The evaluator uses two stacks (values and operators) and processes characters left-to-right.
- Operator precedence and parentheses are handled.
- Uses `double` for numeric storage and computation; results are displayed using default double formatting.
- Division by zero throws an ArithmeticException and the UI will display `Error` and reset the expression.

## Known limitations

- Unary minus / leading negative numbers are not handled (e.g., `-5+2` may lead to an error). Enter values with parentheses like `(0-5)+2` as a workaround.
- No keyboard input support (only mouse button clicks). You can add key bindings to improve UX.
- No persistent history or memory functions.
- Very basic error reporting — invalid expressions display `Error`.
- Floating-point precision is subject to `double` limitations.

## Suggested improvements

- Add support for unary operators (unary minus).
- Add keyboard input and shortcuts.
- Improve error messages to show parse errors.
- Add scientific functions (sin, cos, pow, sqrt) and a memory store (M+, M-, MR).
- Move the class into a named package and provide a proper build system (Maven/Gradle) and unit tests.
- Add localization and improved UI styling.

## Contributing

Contributions are welcome. If you plan to extend the project:
- Create a branch for your feature/fix.
- Prefer adding unit tests for the evaluator.
- If you change package structure, update the README build/run instructions accordingly.

## License

No license file is included in the repository. If you want this project to be open source, add a LICENSE (for example MIT, Apache-2.0, etc.). Until a license is added, the default copyright rules apply.

---

This README was created to document the CalculatorApp found at:
`Project_1/CalculatorApp.java` (source: https://github.com/PranavSRM/Java_Projects/blob/main/Project_1/CalculatorApp.java)
