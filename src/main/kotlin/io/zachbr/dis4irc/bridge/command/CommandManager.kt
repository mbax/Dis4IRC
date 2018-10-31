/*
 * This file is part of Dis4IRC.
 *
 * Dis4IRC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dis4IRC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Dis4IRC.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.zachbr.dis4irc.bridge.command

import io.zachbr.dis4irc.bridge.Bridge
import io.zachbr.dis4irc.bridge.command.api.Executor
import io.zachbr.dis4irc.bridge.command.executors.SystemInfo
import io.zachbr.dis4irc.bridge.message.BOT_SENDER
import io.zachbr.dis4irc.bridge.message.Destination
import io.zachbr.dis4irc.bridge.message.Message

const val COMMAND_PREFIX: String = "!"

/**
 * Responsible for managing, looking up, and delegating to command executors
 */
class CommandManager(private val bridge: Bridge) {
    private val executorsByCommand = HashMap<String, Executor>()
    private val logger = bridge.logger

    init {
        registerExecutor("system", SystemInfo(bridge))
    }

    /**
     * Registers an executor to the given command
     */
    private fun registerExecutor(name: String, executor: Executor) {
        executorsByCommand[name] = executor
    }

    /**
     * Gets the executor for the given command
     */
    private fun getExecutorFor(name: String): Executor? {
        return executorsByCommand[name]
    }

    /**
     * Process a command message, passing it off to the registered executor
     */
    fun processCommandMessage(command: Message) {
        val split = command.contents.split(" ")
        val trigger = split[0].substring(COMMAND_PREFIX.length, split[0].length) // strip off command prefix
        val executor = getExecutorFor(trigger) ?: return

        logger.debug("Passing command to executor: $executor")

        command.destination = Destination.BOTH // we want results to go to both sides by default
        val result = executor.onCommand(command)

        if (result != null) {
            command.contents = result
            command.sender = BOT_SENDER

            // submit as new message
            bridge.submitMessage(command)
        }
    }
}
