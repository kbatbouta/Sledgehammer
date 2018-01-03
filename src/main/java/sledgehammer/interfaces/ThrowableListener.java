/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.interfaces;

/**
 * Listener for Exceptions that are thrown within the scope of the Sledgehammer
 * engine.
 * 
 * @author Jab
 */
public interface ThrowableListener {

	/**
	 * Handles <Throwable>'s thrown within the scope of the Sledgehammer engine.
	 * 
	 * @param reason
	 *            The <String> reason provided by the sitation.
	 * @param throwable
	 *            The <Throwable> thrown.
	 */
	void onError(String reason, Throwable throwable);
}
