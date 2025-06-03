package dev.ocean.valblock.manager;

import dev.ocean.valblock.game.agent.AbstractAgent;
import dev.ocean.valblock.game.agent.impl.*;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class AgentManager {
    
    private final Map<String, AbstractAgent> agents = new ConcurrentHashMap<>();
    private final Map<AbstractAgent.AgentRole, List<AbstractAgent>> agentsByRole = new EnumMap<>(AbstractAgent.AgentRole.class);
    
    public AgentManager() {
        // Initialize role lists
        for (AbstractAgent.AgentRole role : AbstractAgent.AgentRole.values()) {
            agentsByRole.put(role, new ArrayList<>());
        }
    }
    
    /**
     * Register an agent
     */
    public void registerAgent(AbstractAgent agent) {
        agents.put(agent.getName().toLowerCase(), agent);
        agentsByRole.get(agent.getRole()).add(agent);
    }
    
    /**
     * Get agent by name
     */
    public AbstractAgent getAgent(String name) {
        return agents.get(name.toLowerCase());
    }
    
    /**
     * Get all agents
     */
    public Collection<AbstractAgent> getAllAgents() {
        return new ArrayList<>(agents.values());
    }
    
    /**
     * Get agents by role
     */
    public List<AbstractAgent> getAgentsByRole(AbstractAgent.AgentRole role) {
        return new ArrayList<>(agentsByRole.get(role));
    }
    
    /**
     * Check if agent exists
     */
    public boolean hasAgent(String name) {
        return agents.containsKey(name.toLowerCase());
    }
    
    /**
     * Get agents player can use
     */
    public List<AbstractAgent> getAvailableAgents(Player player) {
        return agents.values().stream()
                .filter(agent -> agent.canUse(player))
                .toList();
    }
    
    /**
     * Get random agent
     */
    public AbstractAgent getRandomAgent() {
        List<AbstractAgent> agentList = new ArrayList<>(agents.values());
        if (agentList.isEmpty()) return null;
        return agentList.get(new Random().nextInt(agentList.size()));
    }
    
    /**
     * Get random agent by role
     */
    public AbstractAgent getRandomAgent(AbstractAgent.AgentRole role) {
        List<AbstractAgent> roleAgents = agentsByRole.get(role);
        if (roleAgents.isEmpty()) return null;
        return roleAgents.get(new Random().nextInt(roleAgents.size()));
    }
    
    /**
     * Load default agents
     */
    public void loadDefaultAgents() {
        // Duelists
        registerAgent(new JettAgent());
        registerAgent(new PhoenixAgent());
        registerAgent(new RazeAgent());
        registerAgent(new ReynaAgent());
        
        // Controllers
        registerAgent(new OmenAgent());
        registerAgent(new ViperAgent());
        registerAgent(new BrimstoneAgent());
        registerAgent(new AstraAgent());
        
        // Initiators
        registerAgent(new SovaAgent());
        registerAgent(new BreachAgent());
        registerAgent(new SkyeAgent());
        registerAgent(new KayoAgent());
        
        // Sentinels
        registerAgent(new SageAgent());
        registerAgent(new CypherAgent());
        registerAgent(new KilljoyAgent());
        registerAgent(new ChamberAgent());
        
        dev.ocean.valblock.Plugin.getInstance().getLogger()
                .info("Loaded " + agents.size() + " default agents");
    }
    
    /**
     * Unregister an agent
     */
    public void unregisterAgent(String name) {
        AbstractAgent agent = agents.remove(name.toLowerCase());
        if (agent != null) {
            agentsByRole.get(agent.getRole()).remove(agent);
        }
    }
    
    /**
     * Get agent statistics
     */
    public AgentStats getStats() {
        AgentStats stats = new AgentStats();
        stats.totalAgents = agents.size();
        
        for (AbstractAgent.AgentRole role : AbstractAgent.AgentRole.values()) {
            stats.agentsByRole.put(role, agentsByRole.get(role).size());
        }
        
        return stats;
    }
    
    @Getter
    public static class AgentStats {
        private int totalAgents = 0;
        private Map<AbstractAgent.AgentRole, Integer> agentsByRole = new EnumMap<>(AbstractAgent.AgentRole.class);
    }
}