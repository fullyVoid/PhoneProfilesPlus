package com.stericson.rootshell.execution;

import android.content.Context;

@SuppressWarnings("unused")
public class JavaCommand extends Command
{
    /**
     * Constructor for executing Java commands rather than binaries
     *
     * @param context     needed to execute java command.
     */
    @SuppressWarnings("unused")
    public JavaCommand(int id, Context context, String... command) {
        super(id, command);
        this.context = context;
        this.javaCommand = true;
    }

    /*
      Constructor for executing Java commands rather than binaries

      @param context     needed to execute java command.
     */
//    public JavaCommand(int id, boolean handlerEnabled, Context context, String... command) {
//        super(id, handlerEnabled, command);
//        this.context = context;
//        this.javaCommand = true;
//    }

    /**
     * Constructor for executing Java commands rather than binaries
     *
     * @param context     needed to execute java command.
     */
    @SuppressWarnings("unused")
    public JavaCommand(int id, int timeout, Context context, String... command) {
        super(id, timeout, command);
        this.context = context;
        this.javaCommand = true;
    }

    /*
    @Override
    public void commandOutput(int id, String line)
    {
        super.commandOutput(id, line);
    }
    */

    @SuppressWarnings({"EmptyMethod", "unused"})
    @Override
    public void commandTerminated(int id, String reason)
    {
        // pass
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    @Override
    public void commandCompleted(int id, int exitCode)
    {
        // pass
    }
}
