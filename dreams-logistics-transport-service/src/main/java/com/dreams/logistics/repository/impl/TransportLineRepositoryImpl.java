package com.dreams.logistics.repository.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreams.logistics.entity.line.TransportLine;
import com.dreams.logistics.entity.node.AgencyEntity;
import com.dreams.logistics.entity.node.BaseEntity;
import com.dreams.logistics.model.dto.line.TransportLineNode;
import com.dreams.logistics.model.dto.line.TransportLineSearch;
import com.dreams.logistics.repository.TransportLineRepository;
import com.dreams.logistics.utils.TransportLineUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 对于路线的各种操作
 */
@Component
public class TransportLineRepositoryImpl implements TransportLineRepository {

    @Resource
    private Neo4jClient neo4jClient;

    @Override
    public TransportLineNode findShortestPath(AgencyEntity start, AgencyEntity end) {
        return this.findShortestPath(start, end, 10);
    }

    @Override
    public TransportLineNode findShortestPath(AgencyEntity start, AgencyEntity end, int depth) {
        //获取网点数据在Neo4j中的类型
        String type = AgencyEntity.class.getAnnotation(Node.class).value()[0];
        //构造查询语句
        String cypherQuery = StrUtil.format(
                "MATCH path = shortestPath((start:{}) -[*..{}]-> (end:{}))\n" +
                        "WHERE start.bid = $startId AND end.bid = $endId AND start.status = true AND end.status = true\n" +
                        "RETURN path", type, depth, type);
        Collection<TransportLineNode> transportLineNodeS = this.executeQueryPath(cypherQuery, start, end);
        if (CollUtil.isEmpty(transportLineNodeS)) {
            return null;
        }
        for (TransportLineNode transportLineNode : transportLineNodeS) {
            return transportLineNode;
        }
        return null;
    }

    private List<TransportLineNode> executeQueryPath(String cypherQuery, AgencyEntity start, AgencyEntity end) {
        return ListUtil.toList(this.neo4jClient.query(cypherQuery)
                .bind(start.getBid()).to("startId") //设置参数
                .bind(end.getBid()).to("endId") //设置参数
                .fetchAs(TransportLineNode.class) //设置响应的类型
                .mappedBy((typeSystem, record) -> { //对结果进行封装处理
                    PathValue pathValue = (PathValue) record.get(0);
                    return TransportLineUtils.convert(pathValue);
                }).all());
    }

    @Override
    public List<TransportLineNode> findPathList(AgencyEntity start, AgencyEntity end, int depth, int limit) {
        //获取网点数据在Neo4j中的类型
        String type = AgencyEntity.class.getAnnotation(Node.class).value()[0];
        //构造查询语句
        String cypherQuery = StrUtil.format(
                "MATCH path = (start:{}) -[*..{}]-> (end:{})\n" +
                        "WHERE start.bid = $startId AND end.bid = $endId AND start.status = true AND end.status = true\n" +
                        "UNWIND relationships(path) AS r\n" +
                        "WITH sum(r.cost) AS cost, path\n" +
                        "RETURN path ORDER BY cost ASC, LENGTH(path) ASC LIMIT {}", type, depth, type, limit);
        return this.executeQueryPath(cypherQuery, start, end);
    }

    @Override
    public Long queryCount(BaseEntity firstNode, BaseEntity secondNode) {
        String firstNodeType = firstNode.getClass().getAnnotation(Node.class).value()[0];
        String secondNodeType = secondNode.getClass().getAnnotation(Node.class).value()[0];
        String cypherQuery = StrUtil.format(
                "MATCH (m:{}) -[r]- (n:{})\n" +
                        "WHERE m.bid = $firstBid AND n.bid = $secondBid\n" +
                        "RETURN count(r) AS c", firstNodeType, secondNodeType);
        Optional<Long> optional = this.neo4jClient.query(cypherQuery)
                .bind(firstNode.getBid()).to("firstBid")
                .bind(secondNode.getBid()).to("secondBid")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> Convert.toLong(record.get("c")))
                .one();
        return optional.orElse(0L);
    }

    @Override
    public Long create(BaseEntity firstNode, BaseEntity secondNode, TransportLine transportLine) {
        String firstNodeType = firstNode.getClass().getAnnotation(Node.class).value()[0];
        String secondNodeType = secondNode.getClass().getAnnotation(Node.class).value()[0];
        String cypherQuery = StrUtil.format(
                "MATCH (m:{} {bid : $firstBid})\n" +
                        "WITH m\n" +
                        "MATCH (n:{} {bid : $secondBid})\n" +
                        "WITH m,n\n" +
                        "CREATE\n" +
                        " (m) -[r:IN_LINE {cost:$cost, number:$number, type:$type, name:$name, distance:$distance, time:$time, extra:$extra, startOrganId:$startOrganId, endOrganId:$endOrganId,created:$created, updated:$updated}]-> (n),\n" +
                        " (m) <-[:OUT_LINE {cost:$cost, number:$number, type:$type, name:$name, distance:$distance, time:$time, extra:$extra, startOrganId:$endOrganId, endOrganId:$startOrganId, created:$created, updated:$updated}]- (n)\n" +
                        "RETURN count(r) AS c", firstNodeType, secondNodeType);
        Optional<Long> optional = this.neo4jClient.query(cypherQuery)
                .bindAll(BeanUtil.beanToMap(transportLine))
                .bind(firstNode.getBid()).to("firstBid")
                .bind(secondNode.getBid()).to("secondBid")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> Convert.toLong(record.get("c")))
                .one();
        return optional.orElse(0L);
    }

    @Override
    public Long update(TransportLine transportLine) {
        String cypherQuery = "MATCH () -[r]-> ()\n" +
                "WHERE id(r) = $id\n" +
                "SET r.cost = $cost , r.number = $number, r.name = $name ,r.distance = $distance ,r.time = $time, r.startOrganId = $startOrganId, r.endOrganId = $endOrganId, r.updated = $updated , r.extra = $extra \n" +
                "RETURN count(r) AS c";
        Optional<Long> optional = this.neo4jClient.query(cypherQuery)
                .bindAll(BeanUtil.beanToMap(transportLine))
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> Convert.toLong(record.get("c")))
                .one();
        return optional.orElse(0L);
    }

    @Override
    public Long remove(Long lineId) {
        String cypherQuery = "MATCH () -[r]-> ()\n" +
                "WHERE id(r) = $lineId\n" +
                "DETACH DELETE r\n" +
                "RETURN count(r) AS c";
        Optional<Long> optional = this.neo4jClient.query(cypherQuery)
                .bind(lineId).to("lineId")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> Convert.toLong(record.get("c")))
                .one();
        return optional.orElse(0L);
    }

    @Override
    public Page<TransportLine> queryPageList(TransportLineSearch transportLineSearch) {
        int page = Math.max(transportLineSearch.getPage(), 1);
        int pageSize = transportLineSearch.getPageSize();
        int skip = (page - 1) * pageSize;
        Map<String, Object> searchParam = BeanUtil.beanToMap(transportLineSearch, false, true);
        MapUtil.removeAny(searchParam, "page", "pageSize");
        //构建查询语句，第一个是查询数据，第二个是查询数量
        String[] cyphers = this.buildPageQueryCypher(searchParam);
        String cypherQuery = cyphers[0];

        //数据
        List<TransportLine> list = ListUtil.toList(this.neo4jClient.query(cypherQuery)
                .bind(skip).to("skip")
                .bind(pageSize).to("limit")
                .bindAll(searchParam)
                .fetchAs(TransportLine.class)
                .mappedBy((typeSystem, record) -> {
                    //封装数据
                    return this.toTransportLine(record);
                }).all());

        // 数据总数
        String countCypher = cyphers[1];
        Long total = this.neo4jClient.query(countCypher)
                .bindAll(searchParam)
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> Convert.toLong(record.get("c")))
                .one().orElse(0L);

        Page<TransportLine> linePage = new Page<>();
        linePage.setRecords(list);
        linePage.setTotal(total);
        return linePage;
    }

    private String[] buildPageQueryCypher(Map<String, Object> searchParam) {
        String queryCypher;
        String countCypher;
        if (CollUtil.isEmpty(searchParam)) {
            //无参数
            queryCypher = "MATCH (m) -[r]-> (n) RETURN m,r,n ORDER BY r.created DESC SKIP $skip LIMIT $limit";
            countCypher = "MATCH () -[r]-> () RETURN count(r) AS c";
        } else {
            //有参数
            String cypherPrefix = "MATCH (m) -[r]-> (n)";
            StringBuilder sb = new StringBuilder();
            sb.append(cypherPrefix).append(" WHERE 1=1 ");
            for (String key : searchParam.keySet()) {
                Object value = searchParam.get(key);
                if (value instanceof String) {
                    if (StrUtil.isNotBlank(Convert.toStr(value))) {
                        sb.append(StrUtil.format("AND r.{} CONTAINS ${} \n", key, key));
                    }
                } else {
                    sb.append(StrUtil.format("AND r.{} = ${} \n", key, key));
                }
            }
            String cypher = sb.toString();
            queryCypher = cypher + "RETURN m,r,n ORDER BY r.created DESC SKIP $skip LIMIT $limit";
            countCypher = cypher + "RETURN count(r) AS c";
        }
        return new String[]{queryCypher, countCypher};
    }

    @Override
    public List<TransportLine> queryByIds(Long... ids) {
        String cypherQuery = "MATCH (m) -[r]-> (n)\n" +
                "WHERE id(r) in $ids\n" +
                "RETURN m,r,n";
        return ListUtil.toList(this.neo4jClient.query(cypherQuery)
                .bind(ids).to("ids")
                .fetchAs(TransportLine.class)
                .mappedBy((typeSystem, record) -> {
                    //封装数据
                    return this.toTransportLine(record);
                }).all());
    }

    private TransportLine toTransportLine(Record record) {
        org.neo4j.driver.types.Node startNode = record.get("m").asNode();
        org.neo4j.driver.types.Node endNode = record.get("n").asNode();
        Relationship relationship = record.get("r").asRelationship();
        Map<String, Object> map = relationship.asMap();

        TransportLine transportLine = BeanUtil.toBeanIgnoreError(map, TransportLine.class);
        transportLine.setStartOrganName(startNode.get("name").asString());
        transportLine.setStartOrganId(startNode.get("bid").asLong());
        transportLine.setEndOrganName(endNode.get("name").asString());
        transportLine.setEndOrganId(endNode.get("bid").asLong());
        transportLine.setId(relationship.id());
        return transportLine;
    }

    @Override
    public TransportLine queryById(Long id) {
        List<TransportLine> transportLines = this.queryByIds(id);
        if (CollUtil.isNotEmpty(transportLines)) {
            return transportLines.get(0);
        }
        return null;
    }

}
