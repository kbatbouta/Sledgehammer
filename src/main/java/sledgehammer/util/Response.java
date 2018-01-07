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
package sledgehammer.util;

import sledgehammer.SledgeHammer;
import sledgehammer.enums.LogType;
import sledgehammer.enums.Result;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class Response {

    private String response;
    private Result result;

    private String log;
    private LogType logType = LogType.INFO;
    private boolean logImportant = false;
    private boolean handled = false;

    public Response() {

    }

    public Response(String response, String log, Result result) {
        this.response = response;
        this.log = log;
        this.result = result;
    }

    public Response(Response response) {
        set(response);
    }

    public void set(Response response) {
        // Sets the Response fields. @formatter:off
        this.result       = response.result      ;
        this.response     = response.response    ;
        this.handled      = response.handled     ;
        this.logImportant = response.logImportant;
        this.log          = response.log         ;
        // @formatter:on
    }

    public void set(Result result, String message) {
        this.result = result;
        this.response = message;
        this.setHandled(true);
    }

    public void log(String log) {
        this.log = log;
    }

    public void log(LogType logType, String log) {
        this.log = log;
        this.logType = logType;
    }

    public void deny() {
        this.result = Result.FAILURE;
        this.response = SledgeHammer.instance.getPermissionDeniedMessage();
        this.setHandled(true);
    }

    public void setHandled(boolean flag) {
        this.handled = flag;
    }

    public boolean isHandled() {
        return this.handled;
    }

    public String getResponse() {
        return this.response;
    }

    public void setLoggedImportant(boolean b) {
        this.logImportant = b;
    }

    public boolean getLogImportance() {
        return this.logImportant;
    }

    public String getLogMessage() {
        if (log == null)
            return null;
        if (log.isEmpty())
            return null;
        return this.log;
    }

    public Result getResult() {
        return result;
    }

    public LogType getLogType() {
        return this.logType;
    }
}