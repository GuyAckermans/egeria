/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.openconnectors.governancedaemonconnectors.openlineageconnectors.janusconnector.maingraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.odpi.openmetadata.openconnectors.governancedaemonconnectors.openlineageconnectors.janusconnector.utils.GraphConstants.*;


/**
 * The Open Lineage Services MockGraphGenerator is meant for creating huge lineage data sets, either for performance
 * testing or demoing lineage with realistic data sizes. The user can specify how many tables there will be, columns per
 * table, number of ETL processes, and the number of glossary terms.
 */
public class GraphGenerator {

    private static final Logger log = LoggerFactory.getLogger(GraphGenerator.class);

    private int numberGlossaryTerms;
    private int numberTables;
    private int processesPerFlow;
    private int tablesPerFlow;
    private int columnsPerTable;
    private int numberProcesses;
    private int numberFlows;
    private boolean cyclic;
    private JanusGraph mockGraph;


    public GraphGenerator(JanusGraph mockGraph) {
        this.mockGraph = mockGraph;
        setProperties();
        generate();
    }

    /**
     * The parameters for the graph that is to be generated are hardcoded for now.
     * A "flow" constitutes of a path between columns of different tables, connected together by process nodes.
     * I.E. columnnode1 -> processnode1 -> columnnode2 -> processnode2 -> columnnode3.
     * The length of this path is specified by the number of processes within the flow.
     */
    private void setProperties() {
        this.cyclic = false;
        this.numberGlossaryTerms = 4;
        this.numberFlows = 1;
        this.processesPerFlow = 4;
        this.columnsPerTable = 4;

        this.tablesPerFlow = processesPerFlow + 1;
        if (cyclic) {
            processesPerFlow += 1;
        }
        this.numberProcesses = numberFlows * processesPerFlow;
        this.numberTables = numberFlows * tablesPerFlow;

    }

    private void generate() {
        try {
            generateVerbose();
            log.info("Generated mock graph");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Generate the graph based on the parameters specified in setProperties().
     */
    private void generateVerbose() {
        List<Vertex> columnNodesPerTable;
        List<Vertex> glossaryNodes = new ArrayList<>();
        List<Vertex> tableNodes = new ArrayList<>();
        List<List<Vertex>> columnNodes = new ArrayList<>();

        GraphTraversalSource g = mockGraph.traversal();

        //Create all Glossary Term nodes
        for (int i = 0; i < numberGlossaryTerms; i++) {
            Vertex glossaryVertex = g.addV(NODE_LABEL_GLOSSARYTERM).next();
            addGlossaryTermProperties(i, glossaryVertex);
            glossaryNodes.add(glossaryVertex);
        }

        //Create synonyms
        Vertex glossaryNode0 = glossaryNodes.get(0);
        Vertex glossaryNode1 = glossaryNodes.get(1);
        Vertex glossaryNode2 = glossaryNodes.get(2);
        Vertex glossaryNode3 = glossaryNodes.get(3);

        glossaryNode0.addEdge(EDGE_LABEL_GLOSSARYTERM_TO_GLOSSARYTERM, glossaryNode1);
        glossaryNode1.addEdge(EDGE_LABEL_GLOSSARYTERM_TO_GLOSSARYTERM, glossaryNode2);
        glossaryNode2.addEdge(EDGE_LABEL_GLOSSARYTERM_TO_GLOSSARYTERM, glossaryNode3);
        if(cyclic)
            glossaryNode3.addEdge(EDGE_LABEL_GLOSSARYTERM_TO_GLOSSARYTERM, glossaryNode1);


        //Create all Process nodes
        List<Vertex> processNodes = new ArrayList<>();
        for (int i = 0; i < numberProcesses; i++) {
            Vertex processVertex = g.addV(NODE_LABEL_SUB_PROCESS).next();
            addProcessProperties(i, processVertex);
            processNodes.add(processVertex);
        }

        //Create all Table nodes and a Host node for each table.
        for (int j = 0; j < numberTables; j++) {
            Vertex tableVertex = g.addV(NODE_LABEL_TABLE).next();
            addTableProperties(j, tableVertex);
            tableNodes.add(tableVertex);

            //Create all Column nodes.
            columnNodesPerTable = new ArrayList<>();
            for (int i = 0; i < columnsPerTable; i++) {
                Vertex columnVertex = g.addV(NODE_LABEL_COLUMN).next();
                addColumnProperties(j, i, columnVertex);
                addEdge(EDGE_LABEL_INCLUDED_IN, columnVertex, tableVertex);

                //Randomly connect Column nodes with Glossary Term nodes.
                if (numberGlossaryTerms != 0) {
                    int randomNum = ThreadLocalRandom.current().nextInt(0, numberGlossaryTerms);
                    Vertex glossaryNode = glossaryNodes.get(randomNum);
                    addEdge(EDGE_LABEL_SEMANTIC, columnVertex, glossaryNode);
                }
                columnNodesPerTable.add(columnVertex);
            }
            columnNodes.add(columnNodesPerTable);
        }

        //Create the lineage flows by connecting columns to processes and connecting processes to the columns of the
        // next table.

        //For each flow
        for (int flowIndex = 0; flowIndex < numberFlows; flowIndex++) {
            //For each table in a flow
            for (int tableIndex = 0; tableIndex < tablesPerFlow - 1; tableIndex++) {

                final Vertex process = processNodes.get(flowIndex * processesPerFlow + tableIndex);

                final Vertex table1 = tableNodes.get(flowIndex * tablesPerFlow + tableIndex);
                final Vertex table2 = tableNodes.get(flowIndex * tablesPerFlow + tableIndex + 1);
                System.out.println("Non cyclic index = " + (flowIndex * tablesPerFlow + tableIndex + 1));
                final List<Vertex> columnsOfTable1 = columnNodes.get(flowIndex * tablesPerFlow + tableIndex);
                final List<Vertex> columnsOfTable2 = columnNodes.get(flowIndex * tablesPerFlow + tableIndex + 1);

                addEdge(EDGE_LABEL_TABLE_AND_PROCESS, table1, process);
                addEdge(EDGE_LABEL_TABLE_AND_PROCESS, process, table2);

                //For each column in a table
                for (int columnIndex = 0; columnIndex < columnsPerTable; columnIndex++) {

                    final Vertex column1 = columnsOfTable1.get(columnIndex);
                    final Vertex column2 = columnsOfTable2.get(columnIndex);

                    addEdge(EDGE_LABEL_COLUMN_AND_PROCESS, column1, process);
                    addEdge(EDGE_LABEL_COLUMN_AND_PROCESS, process, column2);
                }
            }
            if (cyclic) {
                int tableIndex = tablesPerFlow - 1;
                final Vertex process = processNodes.get(flowIndex * processesPerFlow + tableIndex);
                System.out.println("cyclic index = " + (flowIndex * tablesPerFlow + tableIndex ));
                final Vertex table1 = tableNodes.get(flowIndex * tablesPerFlow + tableIndex);
                final Vertex table2 = tableNodes.get(flowIndex * tablesPerFlow);

                final List<Vertex> columnsOfTable1 = columnNodes.get(flowIndex * tablesPerFlow + tableIndex);
                final List<Vertex> columnsOfTable2 = columnNodes.get(flowIndex * tablesPerFlow);

                addEdge(EDGE_LABEL_TABLE_AND_PROCESS, table1, process);
                addEdge(EDGE_LABEL_TABLE_AND_PROCESS, process, table2);

                //For each column in a table
                for (int columnIndex = 0; columnIndex < columnsPerTable; columnIndex++) {

                    final Vertex column1 = columnsOfTable1.get(columnIndex);
                    final Vertex column2 = columnsOfTable2.get(columnIndex);

                    addEdge(EDGE_LABEL_COLUMN_AND_PROCESS, column1, process);
                    addEdge(EDGE_LABEL_COLUMN_AND_PROCESS, process, column2);
                }

            }
        }
        g.tx().commit();
    }

    private void addGlossaryTermProperties(int i, Vertex glossaryVertex) {
        glossaryVertex.property(PROPERTY_KEY_ENTITY_GUID, "g" + i);
        glossaryVertex.property(PROPERTY_KEY_NAME_QUALIFIED_NAME, "qualified.name.g" + i);
        glossaryVertex.property(PROPERTY_KEY_DISPLAY_NAME, "g" + i);
    }

    private void addProcessProperties(int i, Vertex processVertex) {
        processVertex.property(PROPERTY_KEY_ENTITY_GUID, "p" + i);
        processVertex.property(PROPERTY_KEY_DISPLAY_NAME, "p" + i);
    }

    private void addTableProperties(int j, Vertex tableVertex) {
        tableVertex.property(PROPERTY_KEY_ENTITY_GUID, "t" + j);
        tableVertex.property(PROPERTY_KEY_NAME_QUALIFIED_NAME, "qualified.name.t" + j);
        tableVertex.property(PROPERTY_KEY_DISPLAY_NAME, "t" + j);
        tableVertex.property(PROPERTY_KEY_GLOSSARY_TERM, "glossary term");
    }

    private void addColumnProperties(int j, int i, Vertex columnVertex) {
        columnVertex.property(PROPERTY_KEY_ENTITY_GUID, "t" + j + "c" + i);
        columnVertex.property(PROPERTY_KEY_NAME_QUALIFIED_NAME, "qualified.name.t" + j + "c" + i);
        columnVertex.property(PROPERTY_KEY_DISPLAY_NAME, "t" + j + "c" + i);
        columnVertex.property(PROPERTY_KEY_GLOSSARY_TERM, "glossary term");
    }

    private void addEdge(String edgeLabel, Vertex fromVertex, Vertex toVertex) {
        Edge edge = fromVertex.addEdge(edgeLabel, toVertex);
        String uuid = UUID.randomUUID().toString();
        edge.property(PROPERTY_KEY_ENTITY_GUID, uuid);
    }

}