package typeChecker;

import AST.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class VariableScope {

    @Nullable private final VariableScope outerScope;
    @NotNull private final Map<String, Variable> identifiers = new HashMap<>();

    VariableScope(@Nullable VariableScope outerScope) {
        this.outerScope = outerScope;
    }

    @Nullable
    VariableScope getOuterScope() {
        return outerScope;
    }

    void declareVariable(@NotNull Variable variable) {
        assert !isVariableDeclared(variable.getName()) : "Variable already declared";
        identifiers.put(variable.getName(), variable);
    }

    boolean isVariableDeclared(@NotNull String name) {
        return identifiers.containsKey(name);
    }

    @Nullable
    Variable lookupVariable(@NotNull String name) {
        return identifiers.get(name);
    }
}
