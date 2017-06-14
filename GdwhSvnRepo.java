package utilities;

import org.apache.logging.log4j.*;

import gui.SvnPathUI;
import utilities.PropertiesFile;


public class GdwhSvnRepo {

    private String svnRepoPath;
    private static Logger logger = LogManager.getLogger(GdwhSvnRepo.class.getName());

    public GdwhSvnRepo() {
        try {
            PropertiesFile propFile = new PropertiesFile();
            this.svnRepoPath = propFile.getPropertyValue("SvnRepositoryPath");

            if (this.svnRepoPath == null || this.svnRepoPath.isEmpty()) {
                SvnPathUI svnPathWindow = new SvnPathUI();
                this.svnRepoPath = svnPathWindow.getUserInputString();
            }
        } catch (Exception e) {
            logger.error("Can't find svn directory " + svnRepoPath);
            logger.error(e.getMessage());
        }
    }

    public void update() {
        String shellOutput;
        String command = "svn update " + this.svnRepoPath;
        shellOutput = ShellExtension.executeCommand(command);
        logger.info(shellOutput);
    }
}
