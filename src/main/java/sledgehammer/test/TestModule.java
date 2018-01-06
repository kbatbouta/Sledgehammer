package sledgehammer.test;

import java.util.Scanner;

import sledgehammer.SledgeHammer;
import sledgehammer.plugin.Module;
import sledgehammer.util.Printable;
import zombie.core.Core;

/**
 * Class designed to handle common operations for testing Modules.
 *
 * @param <M> The Module being tested.
 * @author Jab
 */
public abstract class TestModule<M extends Module> extends Printable {

    /**
     * The Module being tested.
     */
    private M module;

    private Scanner scanner = new Scanner(System.in);

    /**
     * Initializes SledgeHammer operations in a debug setting.
     */
    public void initializeSledgehammer() {
        Core.GameSaveWorld = "servertest";
        SledgeHammer.instance = new SledgeHammer(true);
        SledgeHammer.instance.init();
        M module = createModule();
        if (module != null) {
            setModule(module);
            // SledgeHammer.instance.getPluginManager().registerModule(getModule());
        }
        SledgeHammer.instance.getPluginManager().onLoad(true);
        SledgeHammer.instance.start();
    }

    public void runTest() {
        run();
        SledgeHammer.instance.stop();
    }

    /**
     * Pauses the execution of the test. Often used to observe data-sets affected
     * during the course of the test.
     */
    public void pause() {
        println("Press ENTER to continue the test.");
        do {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!scanner.hasNextLine());
        scanner.nextLine();
    }

    /**
     * @return Returns the Module being tested.
     */
    public M getModule() {
        return this.module;
    }

    /**
     * (Protected Method)
     * <p>
     * Sets the Module being tested.
     *
     * @param module The Module to set.
     */
    protected void setModule(M module) {
        this.module = module;
    }

    /**
     * Use this method to create and assign the Module being tested.
     *
     * @return The Module instance to be tested.
     */
    public abstract M createModule();

    /**
     * This method executes the test.
     */
    public abstract void run();
}
