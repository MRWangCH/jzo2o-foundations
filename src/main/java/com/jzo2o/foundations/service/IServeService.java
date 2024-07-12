package com.jzo2o.foundations.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

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

}
