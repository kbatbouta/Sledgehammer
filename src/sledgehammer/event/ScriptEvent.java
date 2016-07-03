package sledgehammer.event;

import se.krka.kahlua.vm.KahluaTable;
import zombie.iso.IsoGridSquare;

public class ScriptEvent extends Event {

	public static final String ID = "ScriptEvent";

	private String context = null;

	private Object[] arguments = null;

	public ScriptEvent(String context, Object... arguments) {
		super();
		this.context = context;
		this.arguments = arguments;
	}

	public String getContext() {
		return context;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object... arguments) {
		this.arguments = arguments;
	}

	public String getLogMessage() {
		return null;
	}

	public String getID() {
		return ID;
	}

	public static enum Context {
		
		// '#' for implemented in the LuaListener class.
		
		OnGameBoot("OnGameBoot"), //#
		OnPreGameStart("OnPreGameStart"), //#
		OnTick("OnTick"), //#
		OnTickEvenPaused("OnTickEvenPaused"), //#
		OnFETick("OnFETick"),
		OnGameStart("OnGameStart"), //#
		OnCharacterCollide("OnCharacterCollide"), //#
		OnObjectCollide("OnObjectCollide"), //#
		OnPlayerUpdate("OnPlayerUpdate"), //#
		OnZombieUpdate("OnZombieUpdate"), //#
		OnTriggerNPCEvent("OnTriggerNPCEvent"),
		OnMultiTriggerNPCEvent("OnMultiTriggerNPCEvent"),
		OnLoadMapZones("OnLoadMapZones"), //#
		OnAddBuilding("OnAddBuilding"),
		OnCreateLivingCharacter("OnCreateLivingCharacter"),//#
		OnChallengeQuery("OnChallengeQuery"),
		OnFillInventoryObjectContextMenu("OnFillInventoryObjectContextMenu"),
		OnPreFillInventoryObjectContextMenu("OnPreFillInventoryObjectContextMenu"),
		OnFillWorldObjectContextMenu("OnFillWorldObjectContextMenu"),
		OnPreFillWorldObjectContextMenu("OnPreFillWorldObjectContextMenu"),
		OnMakeItem("OnMakeItem"),
		OnWeaponHitCharacter("OnWeaponHitCharacter"),
		OnWeaponSwing("OnWeaponSwing"),
		OnWeaponHitTree("OnWeaponHitTree"),
		OnWeaponSwingHitPoint("OnWeaponSwingHitPoint"),
		OnPlayerCancelTimedAction("OnPlayerCancelTimedAction"),
		OnLoginState("OnLoginState"),
		OnLoginStateSuccess("OnLoginStateSuccess"),
		OnCharacterCreateStats("OnCharacterCreateStats"),
		OnLoadSoundBanks("OnLoadSoundBanks"),
		OnDoTileBuilding("OnDoTileBuilding"),
		OnDoTileBuilding2("OnDoTileBuilding2"), //#
		OnDoTileBuilding3("OnDoTileBuilding3"),
		OnConnectFailed("OnConnectFailed"),
		OnConnected("OnConnected"),
		OnDisconnect("OnDisconnect"),
		OnConnectionStateChanged("OnConnectionStateChanged"),
		OnScoreboardUpdate("OnScoreboardUpdate"),
		OnNewSurvivorGroup("OnNewSurvivorGroup"),
		OnPlayerSetSafehouse("OnPlayerSetSafehouse"),
		OnLoad("OnLoad"),
		AddXP("AddXP"),
		LevelPerk("LevelPerk"),
		OnSave("OnSave"),
		OnMainMenuEnter("OnMainMenuEnter"),
		OnPreMapLoad("OnPreMapLoad"),
		OnMapLoadCreateIsoObject("OnMapLoadCreateIsoObject"),
		OnCreateSurvivor("OnCreateSurvivor"),
		OnCreatePlayer("OnCreatePlayer"),
		OnPlayerDeath("OnPlayerDeath"),
		OnZombieDead("OnZombieDead"),
		OnCharacterMeet("OnCharacterMeet"),
		OnSpawnRegionsLoaded("OnSpawnRegionsLoaded"),
		OnPostMapLoad("OnPostMapLoad"),
		OnAIStateExecute("OnAIStateExecute"),
		OnAIStateEnter("OnAIStateEnter"),
		OnAIStateExit("OnAIStateExit"),
		OnAIStateChange("OnAIStateChange"),
		OnPlayerMove("OnPlayerMove"),
		OnInitWorld("OnInitWorld"),
		OnNewGame("OnNewGame"),
		OnIsoThumpableLoad("OnIsoThumpableLoad"),
		OnIsoThumpableSave("OnIsoThumpableSave"),
		ReuseGridsquare("ReuseGridsquare"),
		LoadGridsquare("LoadGridsquare"),
		EveryTenMinutes("EveryTenMinutes"),
		EveryDays("EveryDays"),
		EveryHours("EveryHours"),
		OnDusk("OnDusk"),
		OnDawn("OnDawn"),
		OnEquipPrimary("OnEquipPrimary"),
		OnEquipSecondary("OnEquipSecondary"),
		OnClothingUpdated("OnClothingUpdated"),
		OnRainStart("OnRainStart"),
		OnRainStop("OnRainStop"),
		OnAmbientSound("OnAmbientSound"),
		OnResetLua("OnResetLua"),
		OnSeeNewRoom("OnSeeNewRoom"),
		OnNewFire("OnNewFire"),
		OnFillContainer("OnFillContainer"),
		OnChangeWeather("OnChangeWeather"),
		OnDestroyIsoThumpable("OnDestroyIsoThumpable"),
		OnPostSave("OnPostSave"),
		OnWaterAmountChange("OnWaterAmountChange"),
		OnClientCommand("OnClientCommand"), //#
		OnContainerUpdate("OnContainerUpdate"),
		OnObjectAdded("OnObjectAdded"),
		onLoadModDataFromServer("onLoadModDataFromServer"),
		OnGameTimeLoaded("OnGameTimeLoaded"),
		OnWorldMessage("OnWorldMessage"),
		SendCustomModData("SendCustomModData"),
		ServerPinged("ServerPinged"),
		OnServerStarted("OnServerStarted"),
		OnLoadedTileDefinitions("OnLoadedTileDefinitions"),
		DoSpecialTooltip("DoSpecialTooltip"),
		OnCoopJoinFailed("OnCoopJoinFailed"),
		OnDeviceText("OnDeviceText"),
		OnRadioInteraction("OnRadioInteraction"),
		OnAcceptInvite("OnAcceptInvite"),
		OnCoopServerMessage("OnCoopServerMessage");
		
		
		// CLIENT_SIDE_COMMANDS==============================================
		//
		// OnRenderUpdate("OnRenderUpdate"),
		// OnPreUIDraw("OnPreUIDraw"),
		// OnPostUIDraw("OnPostUIDraw"),
		// OnKeyPressed("OnKeyPressed"),
		// OnNPCSurvivorUpdate("OnNPCSurvivorUpdate"),
		// OnJoypadActivate("OnJoypadActivate"),
		// OnObjectLeftMouseButtonDown("OnObjectLeftMouseButtonDown"),
		// OnObjectLeftMouseButtonUp("OnObjectLeftMouseButtonUp"),
		// OnObjectRightMouseButtonDown("OnObjectRightMouseButtonDown"),
		// OnObjectRightMouseButtonUp("OnObjectRightMouseButtonUp"),
		// OnMouseMove("OnMouseMove"),
		// OnMouseDown("OnMouseDown"),
		// OnMouseUp("OnMouseUp"),
		// OnRightMouseDown("OnRightMouseDown"),
		// OnRightMouseUp("OnRightMouseUp"),
		// OnPostFloorSquareDraw("OnPostFloorSquareDraw"),
		// OnPostFloorLayerDraw("OnPostFloorLayerDraw"),
		// OnPostTilesSquareDraw("OnPostTilesSquareDraw"),
		// OnPostTileDraw("OnPostTileDraw"),
		// OnPostWallSquareDraw("OnPostWallSquareDraw"),
		// OnPostCharactersSquareDraw("OnPostCharactersSquareDraw"),
		// OnCreateUI("OnCreateUI"),
		// OnRenderTick("OnRenderTick"),
		// OnJoypadActivateUI("OnJoypadActivateUI"),
		// OnResolutionChange("OnResolutionChange"),
		// OnKeyKeepPressed("OnKeyKeepPressed"),
		// OnPostRender("OnPostRender"),
		// OnCustomUIKey("OnCustomUIKey"),
		// OnServerCommand("OnServerCommand"),
		
		private String context;
		
		Context(String context) {
			this.context = context;
		}
		
		public String getContext() {
			return this.context;
		}
		
		public boolean equals(String other) {
			if(other != null) return other.equals(getContext());
			return false;
		}
	}

}
