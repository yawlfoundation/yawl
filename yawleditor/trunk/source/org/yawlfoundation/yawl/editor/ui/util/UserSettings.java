package org.yawlfoundation.yawl.editor.ui.util;

import org.yawlfoundation.yawl.editor.core.util.FileSaveOptions;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Manages the storage of all user settings between runs of the Editor
 * @author Michael Adams
 * @date 13/06/12
 */
public class UserSettings {

    private static final String NODE_PATH = "/org/yawlfoundation/yawl/editor";
    private static final Preferences _prefs = Preferences.userRoot().node(NODE_PATH);

    //screen settings
    private static final String FRAME_WIDTH = "width";
    private static final String FRAME_HEIGHT = "height";
    private static final String FRAME_POS_X = "posX";
    private static final String FRAME_POS_Y = "posY";
    private static final String INTERNAL_FRAME_WIDTH = "internalFrameWidth";
    private static final String INTERNAL_FRAME_HEIGHT = "internalFrameHeight";

    // analysis settings
    private static final String VERIFY_ON_SAVE = "verifyWithExportCheck";
    private static final String ANALYSE_ON_SAVE = "analyseWithExportCheck";
    private static final String AUTO_INCREMENT_VERSION_ON_SAVE = "autoIncVersionExportCheck";
    private static final String FILE_BACKUP_ON_SAVE = "backupOnExportCheck";
    private static final String FILE_VERSIONING_ON_SAVE = "savePreviousOnExportCheck";
    private static final String RESET_NET_ANALYSIS = "resetNetAnalysisCheck";
    private static final String SOUNDNESS_ANALYSIS = "resetSoundnessCheck";
    private static final String WEAK_SOUNDNESS_ANALYSIS = "resetWeakSoundnessCheck";
    private static final String CANCELLATION_ANALYSIS = "resetCancellationCheck";
    private static final String OR_JOIN_ANALYSIS = "resetOrjoinCheck";
    private static final String SHOW_OBSERVATIONS = "resetShowObservationsCheck";
    private static final String USE_YAWL_REDUCTION_RULES = "yawlReductionRules";
    private static final String USE_RESET_REDUCTION_RULES = "resetReductionRules";
    private static final String OR_JOIN_CYCLE_ANALYSIS = "resetOrjoinCycleCheck";
    private static final String WOFYAWL_ANALYSIS = "wofYawlAnalysisCheck";
    private static final String STRUCTURAL_ANALYSIS = "wofYawlStructuralAnalysisCheck";
    private static final String BEHAVIOURAL_ANALYSIS = "wofYawlBehaviouralAnalysisCheck";
    private static final String EXTENDED_COVERABILITY = "wofYawlExtendedCoverabilityCheck";
    private static final String KEEP_ANALYSIS_DIALOG_OPEN = "keepAnalysisDialogOpenWhenDone";

    // engine & resource service settings
    private static final String ENGINE_USERID = "engineUserID";
    private static final String ENGINE_PASSWORD = "engineUserPassword";
    private static final String ENGINE_URI = "engineURI";
    private static final String RESOURCE_USERID = "resourcingServiceUserID";
    private static final String RESOURCE_PASSWORD = "resourcingServiceUserPassword";
    private static final String RESOURCE_URI = "resourcingServiceURI";

    // external file paths settings
    private static final String DECOMPOSITION_ATTRIBUTES_FILE_PATH =
            "ExtendedAttributeDecompositionFilePath";
    private static final String VARIABLE_ATTRIBUTES_FILE_PATH =
            "ExtendedAttributeVariableFilePath";
    private static final String TASK_ICONS_FILE_PATH = "TaskIconsFilePath";
    private static final String WOFYAWL_FILE_PATH = "WofyawlFilePath";
    private static final String WENDY_FILE_PATH = "WendyFilePath";

    // recently opened file list settings
    private static final String OPEN_RECENT_FILE_PREFIX = "openRecent";
    private static int MAX_RECENT_FILES = 8;

    // last file dialog path setting
    private static final String LAST_SAVE_OR_LOAD_PATH = "lastUsedSaveLoadDirectory";


    // canvas view settings
    private static final String SHOW_ANTI_ALIASING = "showAntiAliasing";
    private static final String SHOW_GRID = "showNetGrid";
    private static final String SHOW_TOOL_TIPS = "showToolTips";
    private static final String JOIN_FILL_COLOUR = "joinFillColor";
    private static final String SPLIT_FILL_COLOUR = "splitFillColor";

    // process configuration settings
    private static final String CONFIGURABLE_NEW_ELEMENTS =
            "ProcessConfigNewElementsConfigurable";
    private static final String CONFIGURABLE_AUTO_GREYOUT = "ProcessConfigAutoGrayout";
    private static final String CONFIGURABLE_BLOCKING_INPUT_PORTS =
            "ProcessConfigBlockingInputPorts";
    private static final String CONFIGURABLE_ALLOW_DEFAULT_CHANGES =
            "ProcessConfigAllowDefaultConfigChanges";


    public static Preferences getSettings() { return _prefs; }


    public static void setFrameWidth(int width) {
        _prefs.putInt(FRAME_WIDTH, width);
    }

    public static int getFrameWidth() {
        return _prefs.getInt(FRAME_WIDTH, 500);
    }

    public static void setFrameHeight(int height) {
        _prefs.putInt(FRAME_HEIGHT, height);
    }

    public static int getFrameHeight() {
        return _prefs.getInt(FRAME_HEIGHT, 300);
    }

    public static void setFrameLocation(int x, int y) {
        _prefs.putInt(FRAME_POS_X, x);
        _prefs.putInt(FRAME_POS_Y, y);
    }

    public static Point getFrameLocation() {
        return new Point(_prefs.getInt(FRAME_POS_X, -1), _prefs.getInt(FRAME_POS_Y, -1));
    }

    public static void setInternalFrameWidth(int width) {
        _prefs.putInt(INTERNAL_FRAME_WIDTH, width);
    }

    public static int getInternalFrameWidth() {
        return _prefs.getInt(INTERNAL_FRAME_WIDTH, 500);
    }

    public static void setInternalFrameHeight(int height) {
        _prefs.putInt(INTERNAL_FRAME_HEIGHT, height);
    }

    public static int getInternalFrameHeight() {
        return _prefs.getInt(INTERNAL_FRAME_HEIGHT, 300);
    }


    public static void setVerifyOnSave(boolean verify) {
        setBoolean(VERIFY_ON_SAVE, verify);
    }

    public static boolean getVerifyOnSave() {
        return getBoolean(VERIFY_ON_SAVE);
    }

    public static void setAnalyseOnSave(boolean analyse) {
        setBoolean(ANALYSE_ON_SAVE, analyse);
    }

    public static boolean getAnalyseOnSave() {
        return getBoolean(ANALYSE_ON_SAVE);
    }

    public static void setAutoIncrementVersionOnSave(boolean autoInc) {
        setBoolean(AUTO_INCREMENT_VERSION_ON_SAVE, autoInc);
    }

    public static boolean getAutoIncrementVersionOnSave() {
        return getBoolean(AUTO_INCREMENT_VERSION_ON_SAVE);
    }

    public static void setFileBackupOnSave(boolean backup) {
        setBoolean(FILE_BACKUP_ON_SAVE, backup);
    }

    public static boolean getFileBackupOnSave() {
        return getBoolean(FILE_BACKUP_ON_SAVE);
    }

    public static void setFileVersioningOnSave(boolean versioning) {
        setBoolean(FILE_VERSIONING_ON_SAVE, versioning);
    }

    public static boolean getFileVersioningOnSave() {
        return getBoolean(FILE_VERSIONING_ON_SAVE);
    }

    public static FileSaveOptions getFileSaveOptions() {
        return new FileSaveOptions(getAutoIncrementVersionOnSave(), getFileBackupOnSave(),
                getFileVersioningOnSave(), getVerifyOnSave());
    }

    public static void setResetNetAnalysis(boolean resetNet) {
        setBoolean(RESET_NET_ANALYSIS, resetNet);
    }

    public static boolean getResetNetAnalysis() {
        return getBoolean(RESET_NET_ANALYSIS);
    }

    public static void setSoundnessAnalysis(boolean soundness) {
        setBoolean(SOUNDNESS_ANALYSIS, soundness);
    }

    public static boolean getSoundnessAnalysis() {
        return getBoolean(SOUNDNESS_ANALYSIS);
    }

    public static void setWeakSoundnessAnalysis(boolean weak) {
        setBoolean(WEAK_SOUNDNESS_ANALYSIS, weak);
    }

    public static boolean getWeakSoundnessAnalysis() {
        return getBoolean(WEAK_SOUNDNESS_ANALYSIS);
    }

    public static void setCancellationAnalysis(boolean cancel) {
        setBoolean(CANCELLATION_ANALYSIS, cancel);
    }

    public static boolean getCancellationAnalysis() {
        return getBoolean(CANCELLATION_ANALYSIS);
    }

    public static void setOrJoinAnalysis(boolean orJoin) {
        setBoolean(OR_JOIN_ANALYSIS, orJoin);
    }

    public static boolean getOrJoinAnalysis() {
        return getBoolean(OR_JOIN_ANALYSIS);
    }

    public static void setShowObservations(boolean show) {
        setBoolean(SHOW_OBSERVATIONS, show);
    }

    public static boolean getShowObservations() {
        return getBoolean(SHOW_OBSERVATIONS);
    }

    public static void setUseYawlReductionRules(boolean use) {
        setBoolean(USE_YAWL_REDUCTION_RULES, use);
    }

    public static boolean getUseYawlReductionRules() {
        return getBoolean(USE_YAWL_REDUCTION_RULES);
    }

    public static void setUseResetReductionRules(boolean use) {
        setBoolean(USE_RESET_REDUCTION_RULES, use);
    }

    public static boolean getUseResetReductionRules() {
        return getBoolean(USE_RESET_REDUCTION_RULES);
    }

    public static void setOrJoinCycleAnalysis(boolean cycle) {
        setBoolean(OR_JOIN_CYCLE_ANALYSIS, cycle);
    }

    public static boolean getOrJoinCycleAnalysis() {
        return getBoolean(OR_JOIN_CYCLE_ANALYSIS);
    }

    public static void setWofyawlAnalysis(boolean wofyawl) {
        setBoolean(WOFYAWL_ANALYSIS, wofyawl);
    }

    public static boolean getWofyawlAnalysis() {
        return getBoolean(WOFYAWL_ANALYSIS);
    }

    public static void setStructuralAnalysis(boolean structural) {
        setBoolean(STRUCTURAL_ANALYSIS, structural);
    }

    public static boolean getStructuralAnalysis() {
        return getBoolean(STRUCTURAL_ANALYSIS);
    }

    public static void setBehaviouralAnalysis(boolean behavioural) {
        setBoolean(BEHAVIOURAL_ANALYSIS, behavioural);
    }

    public static boolean getBehaviouralAnalysis() {
        return getBoolean(BEHAVIOURAL_ANALYSIS);
    }

    public static void setExtendedCoverability(boolean extended) {
        setBoolean(EXTENDED_COVERABILITY, extended);
    }

    public static boolean getExtendedCoverability() {
        return getBoolean(EXTENDED_COVERABILITY);
    }

    public static void setKeepAnalysisDialogOpen(boolean keepOpen) {
        setBoolean(KEEP_ANALYSIS_DIALOG_OPEN, keepOpen);
    }

    public static boolean getKeepAnalysisDialogOpen() {
        return getBoolean(KEEP_ANALYSIS_DIALOG_OPEN);
    }


    public static void setEngineUserid(String id) {
        setString(ENGINE_USERID, id);
    }

    public static String getEngineUserid() {
        return getString(ENGINE_USERID);
    }

    public static void setEnginePassword(String password) {
        setString(ENGINE_PASSWORD, password);
    }

    public static String getEnginePassword() {
        return getString(ENGINE_PASSWORD);
    }

    public static void setEngineUri(String uri) {
        setString(ENGINE_URI, uri);
    }

    public static String getEngineUri() {
        return getString(ENGINE_URI);
    }

    public static void setResourceUserid(String id) {
        setString(RESOURCE_USERID, id);
    }

    public static String getResourceUserid() {
        return getString(RESOURCE_USERID);
    }

    public static void setResourcePassword(String password) {
        setString(RESOURCE_PASSWORD, password);
    }

    public static String getResourcePassword() {
        return getString(RESOURCE_PASSWORD);
    }

    public static void setResourceUri(String uri) {
        setString(RESOURCE_URI, uri);
    }

    public static String getResourceUri() {
        return getString(RESOURCE_URI);
    }

    public static void setDecompositionAttributesFilePath(String path) {
        setString(DECOMPOSITION_ATTRIBUTES_FILE_PATH, path);
    }

    public static String getDecompositionAttributesFilePath() {
        return getString(DECOMPOSITION_ATTRIBUTES_FILE_PATH);
    }

    public static void setVariableAttributesFilePath(String path) {
        setString(VARIABLE_ATTRIBUTES_FILE_PATH, path);
    }

    public static String getVariableAttributesFilePath() {
        return getString(VARIABLE_ATTRIBUTES_FILE_PATH);
    }

    public static void setTaskIconsFilePath(String path) {
        setString(TASK_ICONS_FILE_PATH, path);
    }

    public static String getTaskIconsFilePath() {
        return getString(TASK_ICONS_FILE_PATH);
    }

    public static void setWofyawlFilePath(String path) {
        setString(WOFYAWL_FILE_PATH, path);
    }

    public static String getWofyawlFilePath() {
        return getString(WOFYAWL_FILE_PATH);
    }

    public static void setWendyFilePath(String path) {
        setString(WENDY_FILE_PATH, path);
    }

    public static String getWendyFilePath() {
        return getString(WENDY_FILE_PATH);
    }

    public static void setShowAntiAliasing(boolean show) {
        setBoolean(SHOW_ANTI_ALIASING, show);
    }

    public static boolean getShowAntiAliasing() {
        return getBoolean(SHOW_ANTI_ALIASING);
    }

    public static void setShowGrid(boolean show) {
        setBoolean(SHOW_GRID, show);
    }

    public static boolean getShowGrid() {
        return getBoolean(SHOW_GRID);
    }

    public static void setShowToolTips(boolean show) {
        setBoolean(SHOW_TOOL_TIPS, show);
    }

    public static boolean getShowToolTips() {
        return getBoolean(SHOW_TOOL_TIPS);
    }

    public static void setJoinFillColour(Color colour) {
        setColour(JOIN_FILL_COLOUR, colour);
    }

    public static Color getJoinFillColour() {
        return getColour(JOIN_FILL_COLOUR);
    }

    public static void setSplitFillColour(Color colour) {
        setColour(SPLIT_FILL_COLOUR, colour);
    }

    public static Color getSplitFillColour() {
        return getColour(SPLIT_FILL_COLOUR);
    }

    public static void setLastSaveOrLoadPath(String path) {
        setString(LAST_SAVE_OR_LOAD_PATH, path);
    }

    public static String getLastSaveOrLoadPath() {
        return getString(LAST_SAVE_OR_LOAD_PATH);
    }


    public static void setConfigurableAllowDefaultChanges(boolean allow) {
        setBoolean(CONFIGURABLE_ALLOW_DEFAULT_CHANGES, allow);
    }

    public static boolean getConfigurableAllowDefaultChanges() {
        return getBoolean(CONFIGURABLE_ALLOW_DEFAULT_CHANGES);
    }

    public static void setConfigurableNewElements(boolean allow) {
        setBoolean(CONFIGURABLE_NEW_ELEMENTS, allow);
    }

    public static boolean getConfigurableNewElements() {
        return getBoolean(CONFIGURABLE_NEW_ELEMENTS);
    }

    public static void setConfigurableAutoGreyout(boolean allow) {
        setBoolean(CONFIGURABLE_AUTO_GREYOUT, allow);
    }

    public static boolean getConfigurableAutoGreyout() {
        return getBoolean(CONFIGURABLE_AUTO_GREYOUT);
    }

    public static void setConfigurableBlockingInputPorts(boolean allow) {
        setBoolean(CONFIGURABLE_BLOCKING_INPUT_PORTS, allow);
    }

    public static boolean getConfigurableBlockingInputPorts() {
        return getBoolean(CONFIGURABLE_BLOCKING_INPUT_PORTS);
    }


    public static void setMaxRecentFiles(int max) {
        MAX_RECENT_FILES = max;
    }

    public static java.util.List<String> loadRecentFileList() {
        List<String> recentFileList = new ArrayList<String>(MAX_RECENT_FILES);
        for (int i = 0; i < MAX_RECENT_FILES; i++) {
            String fileName = _prefs.get(OPEN_RECENT_FILE_PREFIX + i, null);
            if (fileName == null) fileName="";
            recentFileList.add(fileName);
        }
        return recentFileList;
    }

    public static void pushRecentFile(String fullFileName) {
      List<String> recentList = loadRecentFileList();
      int duplicatePos = recentList.indexOf(fullFileName);
      int start = (duplicatePos > -1) ? duplicatePos - 1 : MAX_RECENT_FILES - 2;
      for (int i = start; i >= 0; i--) {
          String fileName = recentList.get(i);
          if ((fileName != null) && (fileName.length() > 0))  {
              _prefs.put(OPEN_RECENT_FILE_PREFIX + (i+1), fileName);
          }
      }
      _prefs.put(OPEN_RECENT_FILE_PREFIX + 0, fullFileName);
    }

    public static void removeRecentFile(int pos) {
         for (int i = pos; i < MAX_RECENT_FILES - 1; i++) {
             _prefs.put(OPEN_RECENT_FILE_PREFIX + i,
                     _prefs.get(OPEN_RECENT_FILE_PREFIX + (i+1), ""));
         }
         _prefs.put(OPEN_RECENT_FILE_PREFIX + 7, "");
     }


    private static boolean getBoolean(String key) {
        return _prefs.getBoolean(key, false);
    }

    private static void setBoolean(String key, boolean value) {
        _prefs.putBoolean(key, value);
    }

    private static String getString(String key) {
        return _prefs.get(key, null);
    }

    private static void setString(String key, String value) {
        _prefs.put(key, value);
    }

    private static void setColour(String key, Color colour) {
        _prefs.putInt(key, colour.getRGB());
    }

    private static Color getColour(String key) {
        return new Color(_prefs.getInt(key, Color.WHITE.getRGB()));
    }


}
