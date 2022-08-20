package client;

import com.beust.jcommander.Parameter;

public class Arguments {
    @Parameter(names = "-t", description = "Command")
    String command;

    @Parameter(names = "-k", description = "Key")
    String key;

    @Parameter(names = "-v", description = "Value", variableArity = true)
    String value;

    @Parameter(names = "-in", description = "Filename")
    String fileName;
}
