package com.emar.xreport.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.emar.xreport.domain.DateSpan;
import com.emar.xreport.domain.QueryBean;
import com.emar.model.domain.ConfigColumn;
import com.emar.model.domain.TableColumn;
import com.emar.query.ModelUtil;
import com.emar.query.domain.ESQuery;
import com.emar.query.domain.SQLQuery;
import com.emar.query.domain.QueryBase;
import com.emar.model.domain.DataModel;
import com.emar.util.DateUtil;
import com.emar.util.StringUtil;

/**
 * @author Franplk
 *
 */
public abstract class BaseService {

	protected SQLQuery getModelQuery(DataModel dataModel, QueryBean queryBean) {

		SQLQuery query = new SQLQuery();

		setBaseQuery (query, dataModel, queryBean);
		
		/** Add Data Field **/
		query.setDateKey(dataModel.getDateKey());

		/** Add dimension Exclude **/
		ConfigColumn dimColumn = query.getDimColumn();
		String exValue = dimColumn.getExValue();
		if (null != exValue) {
			query.addParam(dimColumn, "neq", exValue);
		}

		/** Add Filter Conditions **/
		Map<String, Map<String, String>> paramMap = queryBean.getParamMap();
		if (null != paramMap) {
			for (Entry<String, Map<String, String>> param : paramMap.entrySet()) {
				String key = param.getKey();
				ConfigColumn column = dataModel.getColumn(key);
				if (null != column) {
					for (Entry<String, String> filter : param.getValue().entrySet()) {
						query.addParam(column, filter.getKey(), filter.getValue());
					}
				}
			}
		}

		/*** Set Query Table */
		String tableName = ModelUtil.getTableName(query, dataModel.getTheme());
		query.setTable(tableName);

		return query;
	}

	protected ESQuery getESQuery(DataModel dataModel, QueryBean queryBean) {
		
		ESQuery query = new ESQuery();
		
		setBaseQuery (query, dataModel, queryBean);
		
		/** Add Filter Conditions **/
		Map<String, Map<String, String>> paramMap = queryBean.getParamMap();
		if (null != paramMap) {
			for (Entry<String, Map<String, String>> param : paramMap.entrySet()) {
				String key = param.getKey();
				for (Entry<String, String> filter : param.getValue().entrySet()) {
					query.addFilter(key, filter.getKey(), filter.getValue());
				}
			}
		}
		
		return query;
	}
	
	protected List<TableColumn> getTableColumn (List<ConfigColumn> configColumns, boolean isES) {
		List<TableColumn> tableColumns = new ArrayList<>();
		for (ConfigColumn column : configColumns) {
			TableColumn tableColumn = column.convert2TableColumn();
			
			if (isES && column.getSorting() == 0) {
				tableColumn.setSortable(false);
			}
			
			tableColumns.add(tableColumn);
			
			String mapName = column.getMapName();
			if (StringUtil.isNotEmpty(mapName)) {
				String mapTitle = column.getMapTitle();
				tableColumns.add(new TableColumn(mapName, mapTitle));
			}
		}
		return tableColumns;
	}
	
	protected boolean isToday(QueryBean queryBean) {
		DateSpan dateSpan = queryBean.getDateSpan();
		return DateUtil.isToday(dateSpan);
	}
	
	protected List<ConfigColumn> getQueryIndex (DataModel dataModel, QueryBean queryBean) {
		List<String> idxList = queryBean.getIdxList();
		return dataModel.getIdxes(idxList);
	}
	
	private void setBaseQuery (QueryBase query, DataModel dataModel, QueryBean queryBean) {
		
		/** Add Data Span **/
		query.setDateSpan(queryBean.getDateSpan());
		
		/** Add Query Pagination **/
		query.setPage(queryBean.getPage());
		
		/** Add Group Dim **/
		query.setDimColumn(dataModel.getDimColumn());
		
		/** Add Indicator **/
		List<String> idxList = queryBean.getIdxList();
		query.setIdxList(dataModel.getIdxes(idxList));
		
		/** Add Order **/
		String sortName = queryBean.getOrderBy();
		String sortType = queryBean.getOrderType();
		ConfigColumn sortField = dataModel.getColumn(sortName);
		if (sortField.getDim() == 0) {
			if (idxList != null && !idxList.contains(sortName)) {
				sortField = dataModel.getColumn(idxList.get(0));
			}
		}
		query.setSortField(sortField);
		query.setSortType(sortType);
	}
}
