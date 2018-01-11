package sledgehammer.module.discord;

import sledgehammer.interfaces.ThrowableListener;

public class DiscordExceptionListener implements ThrowableListener {

    private ModuleDiscord module;

    private long timeSinceLastException = 0L;

    /**
     * 10-Second cooldown time.
     */
    private long cooldownTime = 10000L;

    DiscordExceptionListener(ModuleDiscord instance) {
        module = instance;
    }

    public void onError(String reason, Throwable throwable) {
        // Grab the current time in milli-seconds.
        long timeNow = System.currentTimeMillis();
        // If the delta is greater than OR equal to the cooldownTime.
        if (timeNow - timeSinceLastException >= cooldownTime) {
            // Set the time.
            timeSinceLastException = timeNow;
            final DiscordBot bot = module.getBot();
            if (bot.isConnected()) {
                // Create a mock stack-trace header.
                String message = throwable.getClass().getName() + ": " + throwable.getLocalizedMessage() + ":";
                // Grab the stack.
                StackTraceElement[] stack = throwable.getStackTrace();
                // Open the code block.
                StringBuilder builder = new StringBuilder("```python\n ");
                builder.append(message).append("\n");
                // Add each line of the stack.
                for (StackTraceElement trace : stack) {
                    builder.append("\t").append(trace).append("\n");
                }
                // Close the code block.
                builder.append("```");
                // Finalized String to use for sending the message.
                final String statementFinal = builder.toString();
                // Send each line synchronusly.
                (new Thread(new Runnable() {
                    public void run() {
                        try {
                            bot.getConsoleChannel().sendMessage("WARNING: An Exception has occured!", true).get();
                            bot.getConsoleChannel().sendMessage(statementFinal).get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                })).start();
            }
        }
    }
}
