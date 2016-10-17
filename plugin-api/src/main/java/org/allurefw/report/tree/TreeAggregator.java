package org.allurefw.report.tree;

import org.allurefw.report.Aggregator;
import org.allurefw.report.entity.TestCase;
import org.allurefw.report.entity.TestCaseResult;
import org.allurefw.report.entity.TestRun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.allurefw.report.ReportApiUtils.generateUid;

/**
 * @author charlie (Dmitry Baev).
 */
public abstract class TreeAggregator implements Aggregator<TreeData> {

    @Override
    public Supplier<TreeData> supplier() {
        return TreeData::new;
    }

    public BiConsumer<TreeData, TestCaseResult> accumulator() {
        return (treeData, result) -> {
            treeData.updateStatistic(result);
            treeData.updateTime(result);

            List<WithChildren> currentLevelGroups = Collections.singletonList(treeData);

            for (TreeGroup treeGroup : getGroups(result)) {
                List<WithChildren> nextLevelGroups = new ArrayList<>();
                for (WithChildren currentLevelGroup : currentLevelGroups) {
                    for (String groupName : treeGroup.getGroupNames()) {
                        TestGroupNode groupNode = findGroupByName(groupName, currentLevelGroup.getChildren());
                        groupNode.updateStatistic(result);
                        groupNode.updateTime(result);
                        nextLevelGroups.add(groupNode);
                    }
                }
                currentLevelGroups = nextLevelGroups;
            }
            TestCaseNode testCaseNode = new TestCaseNode()
                    .withUid(result.getUid())
                    .withName(result.getName())
                    .withStatus(result.getStatus())
                    .withTime(result.getTime());
            for (WithChildren currentLevelGroup : currentLevelGroups) {
                currentLevelGroup.getChildren().add(testCaseNode);
            }
        };
    }

    @Override
    public Consumer<TreeData> aggregate(TestRun testRun, TestCase testCase, TestCaseResult result) {
        return treeData -> {
            treeData.updateStatistic(result);
            treeData.updateTime(result);

            List<WithChildren> currentLevelGroups = Collections.singletonList(treeData);

            for (TreeGroup treeGroup : getGroups(result)) {
                List<WithChildren> nextLevelGroups = new ArrayList<>();
                for (WithChildren currentLevelGroup : currentLevelGroups) {
                    for (String groupName : treeGroup.getGroupNames()) {
                        TestGroupNode groupNode = findGroupByName(groupName, currentLevelGroup.getChildren());
                        groupNode.updateStatistic(result);
                        groupNode.updateTime(result);
                        nextLevelGroups.add(groupNode);
                    }
                }
                currentLevelGroups = nextLevelGroups;
            }
            TestCaseNode testCaseNode = new TestCaseNode()
                    .withUid(result.getUid())
                    .withName(result.getName())
                    .withStatus(result.getStatus())
                    .withTime(result.getTime());
            for (WithChildren currentLevelGroup : currentLevelGroups) {
                currentLevelGroup.getChildren().add(testCaseNode);
            }
        };
    }

    protected TestGroupNode findGroupByName(String groupName, List<TreeNode> nodes) {
        return nodes.stream()
                .filter(TestGroupNode.class::isInstance)
                .map(TestGroupNode.class::cast)
                .filter(group -> groupName.equals(group.getName()))
                .findAny()
                .orElseGet(() -> {
                    TestGroupNode newOne = new TestGroupNode()
                            .withName(groupName)
                            .withUid(generateUid());
                    nodes.add(newOne);
                    return newOne;
                });
    }

    protected abstract List<TreeGroup> getGroups(TestCaseResult result);

}
