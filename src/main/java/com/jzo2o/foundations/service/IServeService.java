package com.jzo2o.foundations.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务表 服务类
 * </p>
 *
 * @author wangchuan
 * @since 2024-07-10
 */
public interface IServeService extends IService<Serve> {
    /**
     * 区域服务分页查询分页
     *
     * @param servePageQueryReqDTO
     * @return PageResult
     */
    PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO);

    /**
     * 批量新增区域服务
     *
     * @param serveUpsertReqDTOList
     */
    void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList);

    /**
     * 修改区域服务价格
     * @param id
     * @param price
     * @return
     */
    Serve update(Long id, BigDecimal price);

    /**
     * 区域服务的上架
     * @param id
     * @return
     */
    Serve onSale(Long id);

    /**
     * 区域服务的下架
     * @param id
     */
    Serve offSale(Long id);

    /**
     * 区域服务删除
     * @param id
     */
    void delete(Long id);

    /**
     * 区域服务设置热门
     * @param id
     */
    void onHot(Long id);

    /**
     * 区域服务取消设置热门
     * @param id
     */
    void offHot(Long id);
}
