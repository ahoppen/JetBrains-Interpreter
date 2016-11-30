package backend.utils;

import backend.AST.Stmt;
import org.jetbrains.annotations.NotNull;

public interface ASTConsumer {
    void consumeStmt(@NotNull Stmt stmt);
}
