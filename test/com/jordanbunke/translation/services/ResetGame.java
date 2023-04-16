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
        // TODO - extend for my campaigns & imported campaigns
    }

    private static void resetMain() {
        final Campaign[] mainCampaigns = LevelIO.readCampaignsInFolder(LevelIO.MAIN_CAMPAIGNS_FOLDER);

        for (Campaign world : mainCampaigns)
            resetCampaign(world);
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
