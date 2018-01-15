/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

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
