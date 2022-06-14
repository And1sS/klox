package interpreter

fun importNativeFunctions(environment: Environment) {
    for (function in nativeFunctions) {
        environment.createVariable(function.name, function)
    }
}

private val clockFunction = NativeFunctionValue("clock", 0) {
    NumericValue(System.currentTimeMillis() / 1000.0)
}

private val nativeFunctions: List<NativeFunctionValue> = listOf(
    clockFunction
)
