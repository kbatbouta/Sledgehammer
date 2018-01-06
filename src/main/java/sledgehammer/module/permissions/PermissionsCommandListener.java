package sledgehammer.module.permissions;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.enums.Result;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class PermissionsCommandListener implements CommandListener {

    private ModulePermissions module;

    private Map<String, String> mapPermissionNodes;

    public PermissionsCommandListener(ModulePermissions module) {
        setModule(module);
        mapPermissionNodes = new HashMap<>();
        // @formatter:off
        mapPermissionNodes.put("permissions"                 , "sledgehammer.permissions"                  );
        mapPermissionNodes.put("permissions group"           , "sledgehammer.permissions.group"            );
        mapPermissionNodes.put("permissions group create"    , "sledgehammer.permissions.group.create"     );
        mapPermissionNodes.put("permissions group delete"    , "sledgehammer.permissions.group.delete"     );
        mapPermissionNodes.put("permissions group rename"    , "sledgehammer.permissions.group.rename"     );
        mapPermissionNodes.put("permissions group set"       , "sledgehammer.permissions.group.set"        );
        mapPermissionNodes.put("permissions group set node"  , "sledgehammer.permissions.group.set.context");
        mapPermissionNodes.put("permissions group set parent", "sledgehammer.permissions.group.set.parent" );
        mapPermissionNodes.put("permissions user"            , "sledgehammer.permissions.user"             );
        mapPermissionNodes.put("permissions user create"     , "sledgehammer.permissions.user.create"      );
        mapPermissionNodes.put("permissions user delete"     , "sledgehammer.permissions.user.delete"      );
        mapPermissionNodes.put("permissions user set"        , "sledgehammer.permissions.user.set"         );
        mapPermissionNodes.put("permissions user set node"   , "sledgehammer.permissions.user.set.context" );
        mapPermissionNodes.put("permissions user set group"  , "sledgehammer.permissions.user.set.group"   );
        // @formatter:on
    }

    @Override
    public void onCommand(Command c, Response r) {
        ModulePermissions module = getModule();
        LanguagePackage lang = module.getLanguagePackage();
        Result result = Result.FAILURE;
        Player commander = c.getPlayer();
        Language language = commander.getLanguage();
        String commanderName = commander.getUsername();
        String response = null;
        String command = c.getCommand().toLowerCase();
        String[] args = c.getArguments();
        Response moduleResponse = null;
        if (command.equals("permissions")) {
            if (args.length > 0) {
                command = args[0];
                args = getSubArgs(args, 1);
                if (command.equals("help")) {
                    processHelpMessage(commander, r);
                } else if (command.equals("group")) {

                }
            } else {
                // No sub-commands exist.
                processHelpMessage(commander, r);
            }
        }
    }

    @Override
    public String onTooltip(Player commander, Command c) {
        String command = c.getCommand().toLowerCase();
        if (command.equals("permissions")) {
            Response r = new Response();
            processHelpMessage(commander, r);
            return r.getResponse();
        }
        return null;
    }

    @Override
    public String getPermissionNode(String command) {
        return mapPermissionNodes.get(command.toLowerCase().trim());
    }

    @Override
    public String[] getCommands() {
        return new String[]{"permissions"};
    }

    private void processHelpMessage(Player commander, Response r) {
        ModulePermissions module = getModule();
        LanguagePackage lang = module.getLanguagePackage();
        Language language = commander.getLanguage();
        StringBuilder builder = new StringBuilder();
        builder.append(lang.getString("command_tooltip_permissions_header", language));
        for (String key : mapPermissionNodes.keySet()) {
            // Grab the PermissionNode.
            String permissionNode = mapPermissionNodes.get(key);
            if (commander.hasPermission(permissionNode)) {
                String langString = "command_tooltip_" + key.replaceAll(" ", "_");
                module.println("langString: " + langString);
                String langResult = lang.getString(langString, language);
                if (langResult != null) {
                    langResult = langResult.replaceAll("\n", " <LINE> ");
                    builder.append(" <LINE> ").append(langResult);
                }
            }
        }
        // If the Commander does not have any of the permission nodes for the commands, send him a denied message.
        if (builder.length() == 0) {
            r.set(Result.FAILURE, module.getPermissionDeniedMessage());
            return;
        }
        // If there are any help responses, return the result help string.
        r.set(Result.SUCCESS, builder.toString());
    }

    public ModulePermissions getModule() {
        return module;
    }

    public void setModule(ModulePermissions module) {
        this.module = module;
    }

    public static String[] getSubArgs(String[] args, int index) {
        if (args == null) {
            throw new IllegalArgumentException("Arguments Array provided is null.");
        }
        if (args.length == 0) {
            throw new IllegalArgumentException("Arguments Array provided is empty.");
        }
        if (args.length - index < 0) {
            throw new IllegalArgumentException("index given to start is beyond the last index of the arguments Array provided.");
        }
        String[] ret = new String[args.length - index];
        System.arraycopy(args, index, ret, 0, args.length - index);
        return ret;
    }
}