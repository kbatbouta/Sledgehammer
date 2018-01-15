/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.enums;

/**
 * Enumeration for Project Zomboid's Lua Event types.
 *
 * @author Jab
 */
public enum LuaEvent {
    OnGameBoot,
    OnPreGameStart,
    OnTick,
    OnTickEvenPaused,
    OnFETick,
    OnGameStart,
    OnCharacterCollide,
    OnObjectCollide,
    OnPlayerUpdate,
    OnZombieUpdate,
    OnTriggerNPCEvent,
    OnMultiTriggerNPCEvent,
    OnLoadMapZones,
    OnAddBuilding,
    OnCreateLivingCharacter,
    OnChallengeQuery,
    OnFillInventoryObjectContextMenu,
    OnPreFillInventoryObjectContextMenu,
    OnFillWorldObjectContextMenu,
    OnPreFillWorldObjectContextMenu,
    OnMakeItem,
    OnWeaponHitCharacter,
    OnWeaponSwing,
    OnWeaponHitTree,
    OnWeaponSwingHitPoint,
    OnPlayerCancelTimedAction,
    OnLoginState,
    OnLoginStateSuccess,
    OnCharacterCreateStats,
    OnLoadSoundBanks,
    OnDoTileBuilding,
    OnDoTileBuilding2,
    OnDoTileBuilding3,
    OnConnectFailed,
    OnConnected,
    OnDisconnect,
    OnConnectionStateChanged,
    OnScoreboardUpdate,
    OnNewSurvivorGroup,
    OnPlayerSetSafehouse,
    OnLoad,
    AddXP,
    LevelPerk,
    OnSave,
    OnMainMenuEnter,
    OnPreMapLoad,
    OnMapLoadCreateIsoObject,
    OnCreateSurvivor,
    OnCreatePlayer,
    OnPlayerDeath,
    OnZombieDead,
    OnCharacterMeet,
    OnSpawnRegionsLoaded,
    OnPostMapLoad,
    OnAIStateExecute,
    OnAIStateEnter,
    OnAIStateExit,
    OnAIStateChange,
    OnPlayerMove,
    OnInitWorld,
    OnNewGame,
    OnIsoThumpableLoad,
    OnIsoThumpableSave,
    ReuseGridsquare,
    LoadGridsquare,
    EveryTenMinutes,
    EveryDays,
    EveryHours,
    OnDusk,
    OnDawn,
    OnEquipPrimary,
    OnEquipSecondary,
    OnClothingUpdated,
    OnRainStart,
    OnRainStop,
    OnAmbientSound,
    OnResetLua,
    OnSeeNewRoom,
    OnNewFire,
    OnFillContainer,
    OnChangeWeather,
    OnDestroyIsoThumpable,
    OnPostSave,
    OnWaterAmountChange,
    OnClientCommand,
    OnContainerUpdate,
    OnObjectAdded,
    onLoadModDataFromServer,
    OnGameTimeLoaded,
    OnWorldMessage,
    SendCustomModData,
    ServerPinged,
    OnServerStarted,
    OnLoadedTileDefinitions,
    DoSpecialTooltip,
    OnCoopJoinFailed,
    OnDeviceText,
    OnRadioInteraction,
    OnAcceptInvite,
    OnCoopServerMessage;

    /**
     * @param string The string to test.
     * @return Returns true if the String given equals the name of the enum.
     */
    public boolean is(String string) {
        return name().toLowerCase().equals(string.toLowerCase().trim());
    }
}