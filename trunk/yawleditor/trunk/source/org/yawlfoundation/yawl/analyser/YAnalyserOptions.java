package org.yawlfoundation.yawl.analyser;

/**
 * @author Michael Adams
 * @date 11/05/12
 */
public class YAnalyserOptions {

    private boolean wofStructural;
    private boolean wofBehavioural;
    private boolean wofExtendedCoverabiity;
    private String wofYawlExecutableLocation;

    private boolean resetSoundness;
    private boolean resetWeakSoundness;
    private boolean resetCancellation;
    private boolean resetOrJoin;
    private boolean resetOrjoinCycle;
    private boolean resetReductionRules;
    private boolean yawlReductionRules;
    private boolean resetShowObservations;


    public static YAnalyserOptions newAllDisabledOptions() {
        return new YAnalyserOptions();
    }

    public static YAnalyserOptions newDefaultOptions() {
        YAnalyserOptions defaults = new YAnalyserOptions();
        defaults.enableResetSoundness(true);
        defaults.enableResetOrjoinCycle(true);
        defaults.enableResetReductionRules(true);
        defaults.enableYawlReductionRules(true);
        return defaults;
    }

    public static YAnalyserOptions newAllEnabledOptions() {
        YAnalyserOptions allEnabled = newDefaultOptions();
        allEnabled.enableResetWeakSoundness(true);
        allEnabled.enableResetCancellation(true);
        allEnabled.enableResetOrJoin(true);
        allEnabled.enableResetShowObservations(true);

        allEnabled.enableWofBehavioural(true);
        allEnabled.enableWofExtendedCoverabiity(true);
        allEnabled.enableWofStructural(true);

        return allEnabled;
    }


    public boolean isWofAnalysis() {
        return isWofBehavioural() || isWofExtendedCoverabiity() || isWofStructural();
    }


    public boolean isWofStructural() { return wofStructural; }

    public void enableWofStructural(boolean enable) { wofStructural = enable; }


    public boolean isWofBehavioural() { return wofBehavioural; }

    public void enableWofBehavioural(boolean enable) { wofBehavioural = enable; }


    public boolean isWofExtendedCoverabiity() { return wofExtendedCoverabiity; }

    public void enableWofExtendedCoverabiity(boolean enable) {
        wofExtendedCoverabiity = enable;
    }

    public String getWofYawlExecutableLocation() {
        return wofYawlExecutableLocation;
    }

    public void setWofYawlExecutableLocation(String location) {
        wofYawlExecutableLocation = location;
    }

    public boolean isResetAnalysis() {
        return isResetSoundness() || isResetWeakSoundness() || isResetCancellation() ||
               isResetOrJoin() || isResetOrjoinCycle(); }



    public boolean isResetSoundness() { return resetSoundness; }

    public void enableResetSoundness(boolean enable) {resetSoundness = enable; }


    public boolean isResetWeakSoundness() { return resetWeakSoundness; }

    public void enableResetWeakSoundness(boolean enable) { resetWeakSoundness = enable; }


    public boolean isResetCancellation() { return resetCancellation; }

    public void enableResetCancellation(boolean enable) { resetCancellation = enable; }


    public boolean isResetOrJoin() { return resetOrJoin; }

    public void enableResetOrJoin(boolean enable) { resetOrJoin = enable; }


    public boolean isResetShowObservations() { return resetShowObservations; }

    public void enableResetShowObservations(boolean enable) {
        resetShowObservations = enable;
    }


    public boolean isResetOrjoinCycle() { return resetOrjoinCycle; }

    public void enableResetOrjoinCycle(boolean enable) { resetOrjoinCycle = enable; }


    public boolean isResetReductionRules() { return resetReductionRules; }

    public void enableResetReductionRules(boolean enable) { resetReductionRules = enable; }


    public boolean isYawlReductionRules() { return yawlReductionRules; }

    public void enableYawlReductionRules(boolean enable) {
        yawlReductionRules = enable;
    }

}
