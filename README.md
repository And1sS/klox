## Klox

A simple interpreter of toy programming language called Klox, based on lox.

Language includes features such as variables, functions and classes. </br>
You can check web version at https://lightwood13.github.io/loxjstest/

Some code examples: 
- variables, functions and clock() builtin
```kotlin
fun fib(n) {
    if (n == 0) return 0;
    if (n == 1) return 1;
    return fib(n - 1) + fib(n - 2);
}

var before = clock();
print fib(20);
var after = clock();
print after - before;
```

- loops
```kotlin
var a = 0;
var b = 1;
var isEven = true; // to overcome absence of % operator
for (var i = 0; i < 50; i = i + 1) {
    var sum = a + b;
    print sum;
    if (isEven) {
      a = sum;
    } else {
      b = sum;
    }
    isEven = !isEven;
}
```

- closures
```kotlin
fun makeCounter() {
    var a = 1;
    fun counter() {
        a = a + 1;
        print a;
    }
    return counter;
}

var a = 2;
var counter = makeCounter();
counter();
counter();
```
