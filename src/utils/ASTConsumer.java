package utils;

import AST.Stmt;
import org.jetbrains.annotations.NotNull;

public interface ASTConsumer {
    void consumeStmt(@NotNull Stmt stmt);
}
