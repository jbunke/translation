package com.jordanbunke.translation.services;

import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.io.LevelIO;

public class ResetGame {
    public static void main(String[] args) {
        Fonts.setTypeface(Fonts.Typeface.CLASSIC);
        reset();
    }

    private static void reset() {
        resetTutorial();
        resetMain();
        resetMyLevels();
        resetMyCampaigns();
        resetImported();
    }

    private static void resetMain() {
        final Campaign[] mainCampaigns = LevelIO.readCampaignsInFolder(LevelIO.MAIN_CAMPAIGNS_FOLDER);

        for (Campaign world : mainCampaigns)
            resetCampaign(world, true);
    }

    private static void resetImported() {
        final Campaign[] importedCampaigns = LevelIO.readCampaignsInFolder(LevelIO.IMPORTED_CAMPAIGNS_FOLDER);

        for (Campaign campaign : importedCampaigns)
            resetCampaign(campaign, true);
    }

    private static void resetMyLevels() {
        final Campaign myLevels = LevelIO.readMyLevels();
        resetCampaign(myLevels, false);
    }

    private static void resetMyCampaigns() {
        final Campaign[] myCampaigns = LevelIO.readCampaignsInFolder(LevelIO.MY_CAMPAIGNS_FOLDER);

        for (Campaign campaign : myCampaigns)
            resetCampaign(campaign, true);
    }

    private static void resetTutorial() {
        final Campaign tutorial = LevelIO.readCampaign(LevelIO.TUTORIAL_CAMPAIGN_FOLDER);
        resetCampaign(tutorial, true);
    }

    private static void resetCampaign(final Campaign campaign, final boolean resetLevelsBeaten) {
        LevelIO.writeCampaign(campaign, resetLevelsBeaten);

        for (int i = 0; i < campaign.getLevelCount(); i++) {
            final Level level = campaign.getLevelAt(i);
            level.save(true);
            System.out.println("Saved [ " + campaign.getName() + " ] " + level.getName());
        }
    }
}
