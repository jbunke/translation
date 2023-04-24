package com.jordanbunke.translation.services;

import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.io.LevelIO;

public class ResetGame {
    public static void main(String[] args) {
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
            resetCampaign(world);
    }

    private static void resetImported() {
        final Campaign[] importedCampaigns = LevelIO.readCampaignsInFolder(LevelIO.IMPORTED_CAMPAIGNS_FOLDER);

        for (Campaign campaign : importedCampaigns)
            resetCampaign(campaign);
    }

    private static void resetMyLevels() {
        final Campaign myLevels = LevelIO.readMyLevels();
        resetCampaign(myLevels);
    }

    private static void resetMyCampaigns() {
        final Campaign[] myCampaigns = LevelIO.readCampaignsInFolder(LevelIO.MY_CAMPAIGNS_FOLDER);

        for (Campaign campaign : myCampaigns)
            resetCampaign(campaign);
    }

    private static void resetTutorial() {
        final Campaign tutorial = LevelIO.readCampaign(LevelIO.TUTORIAL_CAMPAIGN_FOLDER);
        resetCampaign(tutorial);
    }

    private static void resetCampaign(final Campaign campaign) {
        LevelIO.writeCampaign(campaign, true);

        for (int i = 0; i < campaign.getLevelCount(); i++) {
            final Level level = campaign.getLevelAt(i);
            level.saveLevel(true);
            System.out.println("Saved [ " + campaign.getName() + " ] " + level.getName());
        }
    }
}
