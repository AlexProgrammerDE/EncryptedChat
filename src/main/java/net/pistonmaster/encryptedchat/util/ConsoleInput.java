package net.pistonmaster.encryptedchat.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;

public class ConsoleInput {
    @Getter
    private final CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
    private String prefixInfo = "";

    public void registerArrowKey() {
        PrintStream customStream = new PrintStream(System.out) {
            @Override
            public void println(String x) {
                super.println("\r" + x);
                System.out.print(prefixInfo + "> ");
            }

            @Override
            public PrintStream printf(String x, Object... args) {
                PrintStream returned = super.printf("\r" + x + "%n", args);
                System.out.print(prefixInfo + "> ");
                return returned;
            }
        };
        System.setOut(customStream);
    }

    public void runReadCommandInput(Predicate<Void> predicate, Scanner consoleScanner) {
        while (predicate.test(null)) {
            try {
                String input = consoleScanner.nextLine();
                if (input.isBlank()) continue;

                try {
                    dispatcher.execute(input, null);
                } catch (CommandSyntaxException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } catch (NoSuchElementException ignored) {
            }
        }
    }

    public void setPrefixInfo(String prefixInfo) {
        this.prefixInfo = prefixInfo;

        System.out.print("\r" + prefixInfo + "> ");
    }
}
