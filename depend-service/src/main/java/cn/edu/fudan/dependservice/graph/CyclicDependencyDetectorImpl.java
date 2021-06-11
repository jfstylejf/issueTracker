package cn.edu.fudan.dependservice.graph;
/*
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.smell.CycleASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CyclicDependencyDetectorImpl implements CyclicDependencyDetector {

	@Autowired
	private CycleASRepository cycleASRepository;

	@Autowired
	private CacheService cache;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private DependsOnRepository dependsOnRepository;

	@Autowired
	private SmellRepository smellRepository;

	public static final int DEFAULT_THRESHOLD_LONGEST_PATH = 3;
	public static final double DEFAULT_THRESHOLD_MINIMUM_RATE = 0.5;

	private Collection<DependsOn> findCycleTypeRelationsBySCC(Cycle<Type> cycle) {
		return cycleASRepository.cycleTypesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCycleFileRelationsBySCC(Cycle<ProjectFile> cycle) {
		return cycleASRepository.cycleFilesBySCC(cycle.getPartition());
	}

	private Collection<DependsOn> findCyclePackageRelationsBySCC(Cycle<Package> cycle) {
		return cycleASRepository.cyclePackagesBySCC(cycle.getPartition());
	}

	@Override
	public Map<Long, List<Cycle<Type>>> queryTypeCyclicDependency() {
		String key = "typeCyclicDependency";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<Cycle<Type>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.TYPE, SmellType.CYCLIC_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<Cycle<Type>> typeCycles = new ArrayList<>();
		int partition = 1;
		for (Smell smell : smells) {
			List<Type> components = new ArrayList<>();
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			for (Node containedNode : containedNodes) {
				components.add((Type) containedNode);
			}
			typeCycles.add(new Cycle<>(partition, components));
			partition ++;
		}
		for(Cycle<Type> typeCycle : typeCycles) {
			Project project = containRelationService.findTypeBelongToProject(typeCycle.getComponents().get(0));
			if (project != null) {
				List<Cycle<Type>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(typeCycle);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<Cycle<ProjectFile>>> queryFileCyclicDependency() {
		String key = "fileCyclicDependency";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<Cycle<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.CYCLIC_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<Cycle<ProjectFile>> fileCycles = new ArrayList<>();
		int partition = 1;
		for (Smell smell : smells) {
			List<ProjectFile> components = new ArrayList<>();
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			for (Node containedNode : containedNodes) {
				components.add((ProjectFile) containedNode);
			}
			fileCycles.add(new Cycle<>(partition, components));
			partition ++;
		}
		for(Cycle<ProjectFile> fileCycle : fileCycles) {
			Project project = containRelationService.findFileBelongToProject(fileCycle.getComponents().get(0));
			if (project != null) {
				List<Cycle<ProjectFile>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileCycle);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<Cycle<Package>>> queryPackageCyclicDependency() {
		String key = "packageCyclicDependency";
		if (cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<Cycle<Package>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE, SmellType.CYCLIC_DEPENDENCY));
		SmellUtils.sortSmellByName(smells);
		List<Cycle<Package>> packageCycles = new ArrayList<>();
		int partition = 1;
		for (Smell smell : smells) {
			List<Package> components = new ArrayList<>();
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			for (Node containedNode : containedNodes) {
				components.add((Package) containedNode);
			}
			packageCycles.add(new Cycle<>(partition, components));
			partition ++;
		}
		for(Cycle<Package> packageCycle : packageCycles) {
			Project project = containRelationService.findPackageBelongToProject(packageCycle.getComponents().get(0));
			if (project != null) {
				List<Cycle<Package>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(packageCycle);
				result.put(project.getId(), temp);
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<Cycle<Type>>> detectTypeCyclicDependency() {
		Map<Long, List<Cycle<Type>>> result = new HashMap<>();
		Collection<Cycle<Type>> cycles = cycleASRepository.typeCycles();
		List<List<Type>> componentsList = new ArrayList<>();
		for (Cycle<Type> cycle : cycles) {
			List<Type> types = new ArrayList<>(cycle.getComponents());
			List<DependsOn> relations = new ArrayList<>(findCycleTypeRelationsBySCC(cycle));
			Map<Type, Integer> indexMap = new HashMap<>();

			//将节点映射为数字
			int index = 0;
			for (Type type : types) {
				indexMap.put(type, index);
				index++;
			}

			//最短路径初始化
			int number = index;
			int[][] distanceMap = new int[number][number];
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < number; j++) {
					distanceMap[i][j] = -1;
				}
				distanceMap[i][i] = 0;
			}

			//沿途节点初始化
			Map<String, Set<Integer>> pathMap = new HashMap<>();
			for (DependsOn relation : relations) {
				int sourceIndex = indexMap.get((Type) relation.getStartNode());
				int targetIndex = indexMap.get((Type) relation.getEndNode());
				distanceMap[sourceIndex][targetIndex] = 1;
				String pathKey = String.join("_", String.valueOf(sourceIndex), String.valueOf(targetIndex));
				Set<Integer> path = new HashSet<>();
				path.add(sourceIndex);
				path.add(targetIndex);
				pathMap.put(pathKey, path);
			}

			//获取结果
			List<Set<Integer>> indexCycles = new ArrayList<>(ShortestPathCycleFilter(distanceMap, pathMap, number, DEFAULT_THRESHOLD_LONGEST_PATH, DEFAULT_THRESHOLD_MINIMUM_RATE));
			for (Set<Integer> indexCycle : indexCycles) {
				List<Type> components = new ArrayList<>();
				for(Integer i : indexCycle) {
					components.add(types.get(i));
				}
				componentsList.add(components);
			}
		}

		//生成结果
		componentsList.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
		int partition = 1;
		for (List<Type> components : componentsList) {
			Cycle<Type> typeCycle = new Cycle<>(partition, components);
			Project project = containRelationService.findTypeBelongToProject(components.get(0));
			if (project != null) {
				List<Cycle<Type>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(typeCycle);
				result.put(project.getId(), temp);
			}
			partition ++;
		}
		return result;
	}

	@Override
	public Map<Long, List<Cycle<ProjectFile>>> detectFileCyclicDependency() {
		Map<Long, List<Cycle<ProjectFile>>> result = new HashMap<>();
		Collection<Cycle<ProjectFile>> cycles = cycleASRepository.fileCycles();
		List<List<ProjectFile>> componentsList = new ArrayList<>();
		for (Cycle<ProjectFile> cycle : cycles) {
			List<ProjectFile> files = new ArrayList<>(cycle.getComponents());
			List<DependsOn> relations = new ArrayList<>(findCycleFileRelationsBySCC(cycle));
			Map<ProjectFile, Integer> indexMap = new HashMap<>();

			//将节点映射为数字
			int index = 0;
			for (ProjectFile file : files) {
				indexMap.put(file, index);
				index++;
			}

			//最短路径初始化
			int number = index;
			int[][] distanceMap = new int[number][number];
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < number; j++) {
					distanceMap[i][j] = -1;
				}
				distanceMap[i][i] = 0;
			}

			//沿途节点初始化
			Map<String, Set<Integer>> pathMap = new HashMap<>();
			for (DependsOn relation : relations) {
				int sourceIndex = indexMap.get((ProjectFile) relation.getStartNode());
				int targetIndex = indexMap.get((ProjectFile) relation.getEndNode());
				distanceMap[sourceIndex][targetIndex] = 1;
				String pathKey = String.join("_", String.valueOf(sourceIndex), String.valueOf(targetIndex));
				Set<Integer> path = new HashSet<>();
				path.add(sourceIndex);
				path.add(targetIndex);
				pathMap.put(pathKey, path);
			}

			//获取结果
			List<Set<Integer>> indexCycles = new ArrayList<>(ShortestPathCycleFilter(distanceMap, pathMap, number, DEFAULT_THRESHOLD_LONGEST_PATH, DEFAULT_THRESHOLD_MINIMUM_RATE));
			for (Set<Integer> indexCycle : indexCycles) {
				List<ProjectFile> components = new ArrayList<>();
				for(Integer i : indexCycle) {
					components.add(files.get(i));
				}
				componentsList.add(components);
			}
		}

		//生成结果
		componentsList.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
		int partition = 1;
		for (List<ProjectFile> components : componentsList) {
			Cycle<ProjectFile> fileCycle = new Cycle<>(partition, components);
			Project project = containRelationService.findFileBelongToProject(components.get(0));
			if (project != null) {
				List<Cycle<ProjectFile>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileCycle);
				result.put(project.getId(), temp);
			}
			partition ++;
		}
		return result;
	}

	@Override
	public Map<Long, List<Cycle<Package>>> detectPackageCyclicDependency() {
		Map<Long, List<Cycle<Package>>> result = new HashMap<>();
		Collection<Cycle<Package>> cycles = cycleASRepository.packageCycles();
		List<List<Package>> componentsList = new ArrayList<>();
		for (Cycle<Package> cycle : cycles) {
			List<Package> files = new ArrayList<>(cycle.getComponents());
			List<DependsOn> relations = new ArrayList<>(findCyclePackageRelationsBySCC(cycle));
			Map<Package, Integer> indexMap = new HashMap<>();

			//将节点映射为数字
			int index = 0;
			for (Package file : files) {
				indexMap.put(file, index);
				index++;
			}

			//最短路径初始化
			int number = index;
			int[][] distanceMap = new int[number][number];
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < number; j++) {
					distanceMap[i][j] = -1;
				}
				distanceMap[i][i] = 0;
			}

			//沿途节点初始化
			Map<String, Set<Integer>> pathMap = new HashMap<>();
			for (DependsOn relation : relations) {
				int sourceIndex = indexMap.get((Package) relation.getStartNode());
				int targetIndex = indexMap.get((Package) relation.getEndNode());
				distanceMap[sourceIndex][targetIndex] = 1;
				String pathKey = String.join("_", String.valueOf(sourceIndex), String.valueOf(targetIndex));
				Set<Integer> path = new HashSet<>();
				path.add(sourceIndex);
				path.add(targetIndex);
				pathMap.put(pathKey, path);
			}

			//获取结果
			List<Set<Integer>> indexCycles = new ArrayList<>(ShortestPathCycleFilter(distanceMap, pathMap, number, DEFAULT_THRESHOLD_LONGEST_PATH, DEFAULT_THRESHOLD_MINIMUM_RATE));
			for (Set<Integer> indexCycle : indexCycles) {
				List<Package> components = new ArrayList<>();
				for(Integer i : indexCycle) {
					components.add(files.get(i));
				}
				componentsList.add(components);
			}
		}

		//生成结果
		componentsList.sort((c1, c2) -> Integer.compare(c2.size(), c1.size()));
		int partition = 1;
		for (List<Package> components : componentsList) {
			Cycle<Package> packageCycle = new Cycle<>(partition, components);
			Project project = containRelationService.findPackageBelongToProject(components.get(0));
			if (project != null) {
				List<Cycle<Package>> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(packageCycle);
				result.put(project.getId(), temp);
			}
			partition ++;
		}
		return result;
	}

	@Override
	public JSONObject getTypeCyclicDependencyJson(Long projectId, String smellName) {
		JSONObject result = new JSONObject();
		JSONArray nodesJson = new JSONArray();
		JSONArray edgesJson = new JSONArray();
		JSONArray smellsJson = new JSONArray();
		Smell smell = smellRepository.findProjectSmellsByName(projectId, smellName);
		List<Type> types = new ArrayList<>();
		if (smell != null) {
			String key = smell.getId().toString();
			if (cache.get(getClass(), key) != null) {
				return cache.get(getClass(), key);
			}
			JSONObject smellJson = new JSONObject();
			smellJson.put("name", smell.getName());
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			List<Type> smellTypes = new ArrayList<>();
			for (Node containedNode : containedNodes) {
				smellTypes.add((Type) containedNode);
			}
			JSONArray smellNodesJson = new JSONArray();
			for (Type smellType : smellTypes) {
				if (!types.contains(smellType)) {
					types.add(smellType);
				}
				JSONObject smellNodeJson = new JSONObject();
				smellNodeJson.put("index", types.indexOf(smellType) + 1);
				smellNodeJson.put("path", smellType.getIdentifier());
				smellNodesJson.add(smellNodeJson);
			}
			smellJson.put("nodes", smellNodesJson);
			smellsJson.add(smellJson);
		}
		int length = types.size();
		for (int i = 0; i < length; i ++) {
			Type sourceType = types.get(i);
			JSONObject nodeJson = new JSONObject();
			nodeJson.put("id", sourceType.getId().toString());
			nodeJson.put("name", sourceType.getSimpleName());
			nodeJson.put("path", sourceType.getIdentifier());
			nodeJson.put("label", i + 1);
			nodeJson.put("size", SmellUtils.getSizeOfNodeByLoc(sourceType.getEndLine() - sourceType.getStartLine()));
			nodesJson.add(nodeJson);
			for (int j = 0 ; j < length; j ++) {
				Type targetType = types.get(j);
				if (i != j) {
					DependsOn dependsOn = dependsOnRepository.findDependsOnBetweenTypes(sourceType.getId(), targetType.getId());
					if (dependsOn != null) {
						JSONObject edgeJson = new JSONObject();
						edgeJson.put("id", dependsOn.getId().toString());
						edgeJson.put("source", sourceType.getId().toString());
						edgeJson.put("target", targetType.getId().toString());
						edgeJson.put("source_name", sourceType.getSimpleName());
						edgeJson.put("target_name", targetType.getSimpleName());
						edgeJson.put("source_label", i + 1);
						edgeJson.put("target_label", j + 1);
						edgeJson.put("times", dependsOn.getTimes());
						edgeJson.put("dependsOnTypes", dependsOn.getDependsOnTypes());
						edgesJson.add(edgeJson);
					}
				}
			}
		}
		result.put("smellType", SmellType.CYCLIC_DEPENDENCY);
		result.put("coreNode", "0");
		result.put("nodes", nodesJson);
		result.put("edges", edgesJson);
		result.put("smells", smellsJson);
		if (smell != null) {
			String key = smell.getId().toString();
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	@Override
	public JSONObject getFileCyclicDependencyJson(Long projectId, String smellName) {
		Smell smell = smellRepository.findProjectSmellsByName(projectId, smellName);
		return getFileCyclicDependencyJson(smell);
	}

	@Override
	public JSONObject getPackageCyclicDependencyJson(Long projectId, String smellName) {
		JSONObject result = new JSONObject();
		JSONArray comboArray = new JSONArray();
		JSONArray nodeArray = new JSONArray();
		JSONArray edgeArray = new JSONArray();
		JSONArray smellArray = new JSONArray();
		Smell smell = smellRepository.findProjectSmellsByName(projectId, smellName);
		List<Package> comboList = new ArrayList<>();
		List<ProjectFile> nodeList = new ArrayList<>();
		if (smell != null) {
			String key = smell.getId().toString();
			if (cache.get(getClass(), key) != null) {
				return cache.get(getClass(), key);
			}
			JSONObject smellObject = new JSONObject();
			smellObject.put("name", smell.getName());
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			for (Node containedNode : containedNodes) {
				comboList.add((Package) containedNode);
			}
			JSONArray smellNodeArray = new JSONArray();
			for (Package combo : comboList) {
				JSONObject smellNodeObject = new JSONObject();
				smellNodeObject.put("index", comboList.indexOf(combo) + 1);
				smellNodeObject.put("path", combo.getDirectoryPath());
				smellNodeArray.add(smellNodeObject);
			}
			smellObject.put("nodes", smellNodeArray);
			smellArray.add(smellObject);
		}
		int length = comboList.size();
		for (int i = 0; i < length; i ++) {
			Package sourceCombo = comboList.get(i);
			for (int j = 0 ; j < length; j ++) {
				Package targetCombo = comboList.get(j);
				if (i != j) {
					DependsOn comboToComboDependsOn = dependsOnRepository.findDependsOnBetweenPackages(sourceCombo.getId(), targetCombo.getId());
					if (comboToComboDependsOn != null) {
						JSONObject edgeObject;
						Set<ProjectFile> sourceNodeList = new HashSet<>(dependsOnRepository.findDependsOnSourceFilesBetweenPackages(sourceCombo.getId(), targetCombo.getId()));
						Set<ProjectFile> targetNodeList = new HashSet<>(dependsOnRepository.findDependsOnTargetFilesBetweenPackages(sourceCombo.getId(), targetCombo.getId()));
						for (ProjectFile sourceNode : sourceNodeList) {
							if (!nodeList.contains(sourceNode)) {
								nodeList.add(sourceNode);
							}
						}
						for (ProjectFile targetNode : targetNodeList) {
							if (!nodeList.contains(targetNode)) {
								nodeList.add(targetNode);
							}
						}
						for (ProjectFile sourceNode : sourceNodeList) {
							for (ProjectFile targetNode : targetNodeList) {
								DependsOn nodeToNodeDependsOn = dependsOnRepository.findDependsOnBetweenFiles(sourceNode.getId(), targetNode.getId());
								if (nodeToNodeDependsOn != null) {
									edgeObject = new JSONObject();
									edgeObject.put("id", nodeToNodeDependsOn.getId().toString());
									edgeObject.put("source", sourceNode.getId().toString());
									edgeObject.put("target", targetNode.getId().toString());
									edgeObject.put("source_name", sourceNode.getName());
									edgeObject.put("target_name", targetNode.getName());
									edgeObject.put("source_label", nodeList.indexOf(sourceNode) + 1 + length);
									edgeObject.put("target_label", nodeList.indexOf(targetNode) + 1 + length);
									edgeObject.put("times", nodeToNodeDependsOn.getTimes());
									edgeObject.put("dependsOnTypes", nodeToNodeDependsOn.getDependsOnTypes());
									edgeArray.add(edgeObject);
								}
							}
						}
					}
				}
			}
		}
		int index = 1;
		for (Package combo : comboList) {
			JSONObject comboObject = new JSONObject();
			comboObject.put("id", combo.getId().toString());
			comboObject.put("name", combo.getName());
			comboObject.put("path", combo.getDirectoryPath());
			comboObject.put("label", index ++);
			comboObject.put("size", SmellUtils.getSizeOfNodeByLoc(combo.getLoc()));
			comboObject.put("collapsed", false);
			comboArray.add(comboObject);
		}
		for (ProjectFile node : nodeList) {
			JSONObject nodeObject = new JSONObject();
			nodeObject.put("id", node.getId().toString());
			nodeObject.put("name", node.getName());
			nodeObject.put("path", node.getPath());
			nodeObject.put("label", index ++);
			nodeObject.put("size", SmellUtils.getSizeOfNodeByLoc(node.getLoc()));
			Package combo = containRelationService.findFileBelongToPackage(node);
			if (combo != null) {
				nodeObject.put("comboId", combo.getId().toString());
			}
			nodeArray.add(nodeObject);
		}
		result.put("smellType", SmellType.CYCLIC_DEPENDENCY);
		result.put("coreNode", "0");
		result.put("combos", comboArray);
		result.put("nodes", nodeArray);
		result.put("edges", edgeArray);
		result.put("smells", smellArray);
		if (smell != null) {
			String key = smell.getId().toString();
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	@Override
	public JSONObject getFileCyclicDependencyJson(Long smellId) {
		Smell smell = smellRepository.findSmell(smellId);
		return getFileCyclicDependencyJson(smell);
	}

	public JSONObject getFileCyclicDependencyJson(Smell smell) {
		JSONObject result = new JSONObject();
		JSONArray nodesJson = new JSONArray();
		JSONArray edgesJson = new JSONArray();
		JSONArray smellsJson = new JSONArray();
		List<ProjectFile> files = new ArrayList<>();
		if (smell != null) {
			String key = smell.getId().toString();
			if (cache.get(getClass(), key) != null) {
				return cache.get(getClass(), key);
			}
			JSONObject smellJson = new JSONObject();
			smellJson.put("name", smell.getName());
			Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			List<ProjectFile> smellFiles = new ArrayList<>();
			for (Node containedNode : containedNodes) {
				smellFiles.add((ProjectFile) containedNode);
			}
			JSONArray smellFilesJson = new JSONArray();
			for (ProjectFile smellFile : smellFiles) {
				if (!files.contains(smellFile)) {
					files.add(smellFile);
				}
				JSONObject smellFileJson = new JSONObject();
				smellFileJson.put("index", files.indexOf(smellFile) + 1);
				smellFileJson.put("path", smellFile.getPath());
				smellFilesJson.add(smellFileJson);
			}
			smellJson.put("nodes", smellFilesJson);
			smellsJson.add(smellJson);
		}
		int length = files.size();
		for (int i = 0; i < length; i ++) {
			ProjectFile sourceFile = files.get(i);
			JSONObject nodeJson = new JSONObject();
			nodeJson.put("id", sourceFile.getId().toString());
			nodeJson.put("name", sourceFile.getName());
			nodeJson.put("path", sourceFile.getPath());
			nodeJson.put("label", i + 1);
			nodeJson.put("size", SmellUtils.getSizeOfNodeByLoc(sourceFile.getLoc()));
			nodesJson.add(nodeJson);
			for (int j = 0 ; j < length; j ++) {
				ProjectFile targetFile = files.get(j);
				if (i != j) {
					DependsOn dependsOn = dependsOnRepository.findDependsOnBetweenFiles(sourceFile.getId(), targetFile.getId());
					if (dependsOn != null) {
						JSONObject edgeJson = new JSONObject();
						edgeJson.put("id", dependsOn.getId().toString());
						edgeJson.put("source", sourceFile.getId().toString());
						edgeJson.put("target", targetFile.getId().toString());
						edgeJson.put("source_name", sourceFile.getName());
						edgeJson.put("target_name", targetFile.getName());
						edgeJson.put("source_label", i + 1);
						edgeJson.put("target_label", j + 1);
						edgeJson.put("times", dependsOn.getTimes());
						edgeJson.put("dependsOnTypes", dependsOn.getDependsOnTypes());
						edgesJson.add(edgeJson);
					}
				}
			}
		}
		result.put("smellType", SmellType.CYCLIC_DEPENDENCY);
		result.put("coreNode", "0");
		result.put("nodes", nodesJson);
		result.put("edges", edgesJson);
		result.put("smells", smellsJson);
		if (smell != null) {
			String key = smell.getId().toString();
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	@Override
	public void exportCycleDependency() {
		List<Project> projects = nodeService.allProjects();
		Map<Long, List<Cycle<Type>>> cycleTypes = detectTypeCyclicDependency();
		Map<Long, List<Cycle<ProjectFile>>> cycleFiles = detectFileCyclicDependency();
		Map<Long, List<Cycle<Package>>> cyclePackages = detectPackageCyclicDependency();
		for (Project project : projects) {
			try {
				exportPackageCycleDependency(project, cycleTypes.get(project.getId()), cycleFiles.get(project.getId()), cyclePackages.get(project.getId()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void exportPackageCycleDependency(Project project, List<Cycle<Type>> cycleTypes, List<Cycle<ProjectFile>> cycleFiles, List<Cycle<Package>> cyclePackages) {
		Workbook workbook = new XSSFWorkbook();
		exportTypeCycleDependency(workbook, cycleTypes);
		exportFileCycleDependency(workbook, cycleFiles);
		exportPackageCycleDependency(workbook, cyclePackages);
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream("Cycle_Dependency_" + project.getName() + "(" + project.getLanguage() + ")" + ".xlsx");
			workbook.write(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void exportTypeCycleDependency(Workbook workbook, List<Cycle<Type>> cycleTypes) {
		Sheet sheet = workbook.createSheet("Types");
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Row row = sheet.createRow(rowKey.get());
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("Partition");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("Types");
		cell.setCellStyle(style);
		int startRow;
		int endRow;
		int startCol;
		int endCol;
		for (Cycle<Type> cycleType : cycleTypes) {
			startRow = rowKey.get() + 1;
			Collection<Type> types = cycleType.getComponents();
			for (Type type : types) {
				rowKey.set(rowKey.get() + 1);
				row = sheet.createRow(rowKey.get());
				cell = row.createCell(0);
				cell.setCellValue(cycleType.getPartition());
				style.setAlignment(HorizontalAlignment.CENTER);
				cell.setCellStyle(style);
				cell = row.createCell(1);
				cell.setCellValue(type.getName());
				style.setAlignment(HorizontalAlignment.LEFT);
				cell.setCellStyle(style);
			}
			endRow = rowKey.get();
			startCol = 0;
			endCol = 0;
			CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
			sheet.addMergedRegion(region);
		}
	}

	public void exportFileCycleDependency(Workbook workbook, List<Cycle<ProjectFile>> cycleFiles) {
		Sheet sheet = workbook.createSheet("Files");
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Row row = sheet.createRow(rowKey.get());
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("Partition");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("Files");
		cell.setCellStyle(style);
		int startRow;
		int endRow;
		int startCol;
		int endCol;
		for (Cycle<ProjectFile> cycleFile : cycleFiles) {
			startRow = rowKey.get() + 1;
			Collection<ProjectFile> files = cycleFile.getComponents();
			for (ProjectFile file : files) {
				rowKey.set(rowKey.get() + 1);
				row = sheet.createRow(rowKey.get());
				cell = row.createCell(0);
				cell.setCellValue(cycleFile.getPartition());
				style.setAlignment(HorizontalAlignment.CENTER);
				cell.setCellStyle(style);
				cell = row.createCell(1);
				cell.setCellValue(file.getPath());
				style.setAlignment(HorizontalAlignment.LEFT);
				cell.setCellStyle(style);
			}
			endRow = rowKey.get();
			startCol = 0;
			endCol = 0;
			CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
			sheet.addMergedRegion(region);
		}
	}

	public void exportPackageCycleDependency(Workbook workbook, List<Cycle<Package>> cyclePackages) {
		Sheet sheet = workbook.createSheet("Modules");
		ThreadLocal<Integer> rowKey = new ThreadLocal<>();
		rowKey.set(0);
		Row row = sheet.createRow(rowKey.get());
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		Cell cell;
		cell = row.createCell(0);
		cell.setCellValue("Partition");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("Modules");
		cell.setCellStyle(style);
		int startRow;
		int endRow;
		int startCol;
		int endCol;
		for (Cycle<Package> cyclePackage : cyclePackages) {
			startRow = rowKey.get() + 1;
			Collection<Package> packages = cyclePackage.getComponents();
			for (Package pck : packages) {
				rowKey.set(rowKey.get() + 1);
				row = sheet.createRow(rowKey.get());
				cell = row.createCell(0);
				cell.setCellValue(cyclePackage.getPartition());
				style.setAlignment(HorizontalAlignment.CENTER);
				cell.setCellStyle(style);
				cell = row.createCell(1);
				cell.setCellValue(pck.getName());
				style.setAlignment(HorizontalAlignment.LEFT);
				cell.setCellStyle(style);
			}
			endRow = rowKey.get();
			startCol = 0;
			endCol = 0;
			CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
			sheet.addMergedRegion(region);
		}
	}

	//计算最短路径，并记录沿途节点，对结果进行合并，以此得到Cycles
	private List<Set<Integer>> ShortestPathCycleFilter(int[][] distanceMap, Map<String, Set<Integer>> pathMap, int number, int longestPath, double minimumRate) {
		List<Set<Integer>> result = new ArrayList<>();
		List<Set<Integer>> indexCycles = new ArrayList<>();

		//计算最短路径，并记录沿途节点
		for (int k = 0; k < number; k ++) {
			for (int i = 0; i < number; i ++) {
				for (int j = 0; j < number; j ++) {
					if (distanceMap[i][k] != -1 && distanceMap[k][j] != -1) {
						if (distanceMap[i][j] == -1 || distanceMap[i][j] > distanceMap[i][k] + distanceMap[k][j]) {
							distanceMap[i][j] = distanceMap[i][k] + distanceMap[k][j];
							String keyOfIJ = String.join("_", String.valueOf(i), String.valueOf(j));
							String keyOfIK = String.join("_", String.valueOf(i), String.valueOf(k));
							String keyOfKJ = String.join("_", String.valueOf(k), String.valueOf(j));
							Set<Integer> pathOfIJ = new HashSet<>();
							Set<Integer> pathOfIK = pathMap.getOrDefault(keyOfIK, new HashSet<>());
							Set<Integer> pathOfKJ = pathMap.getOrDefault(keyOfKJ, new HashSet<>());
							pathOfIJ.addAll(pathOfIK);
							pathOfIJ.addAll(pathOfKJ);
							pathMap.put(keyOfIJ, pathOfIJ);
						}
					}
				}
			}
		}

		//筛选满足最长路径条件的环
		for (int i = 0; i < number; i ++) {
			for (int j = i + 1; j < number; j++) {
				if (distanceMap[i][j] > 0 && distanceMap[j][i] > 0 && distanceMap[i][j] + distanceMap[j][i] <= longestPath) {
					String keyOfIJ = String.join("_", String.valueOf(i), String.valueOf(j));
					String keyOfJI = String.join("_", String.valueOf(j), String.valueOf(i));
					Set<Integer> path = new HashSet<>();
					path.addAll(pathMap.getOrDefault(keyOfIJ, new HashSet<>()));
					path.addAll(pathMap.getOrDefault(keyOfJI, new HashSet<>()));
					indexCycles.add(path);
				}
			}
		}

		//融合
		int mergeCount;
		int length = indexCycles.size();
		do {
			mergeCount = 0;
			for (int i = 0; i < length; i ++) {
				Set<Integer> firstCycle = new HashSet<>(indexCycles.get(i));
				if(firstCycle.size() == 0) {
					continue;
				}
				for (int j = i + 1; j < length; j ++) {
					Set<Integer> secondCycle = new HashSet<>(indexCycles.get(j));
					if(secondCycle.size() == 0) {
						continue;
					}
					Set<Integer> mergeCycle = new HashSet<>(firstCycle);
					mergeCycle.retainAll(secondCycle);
					double firstSimilarity = ((double) mergeCycle.size() / (double) firstCycle.size());
					double secondSimilarity = ((double) mergeCycle.size() / (double) secondCycle.size());
					if (firstSimilarity >= minimumRate || secondSimilarity >= minimumRate) {
						firstCycle.addAll(secondCycle);
						indexCycles.set(i, firstCycle);
						indexCycles.set(j, new HashSet<>());
						mergeCount ++;
					}
				}
			}
		}
		while(mergeCount > 0);

		//去空
		for (Set<Integer> indexCycle : indexCycles) {
			if(indexCycle.size() == 0) {
				continue;
			}
			result.add(indexCycle);
		}

		return result;
	}
}

 */
