package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务表 服务实现类
 * </p>
 *
 * @author wangchuan
 * @since 2024-07-10
 */
@Service
public class ServeServiceImpl extends ServiceImpl<ServeMapper, Serve> implements IServeService {

    @Resource
    private ServeItemMapper serveItemMapper;

    @Resource
    private RegionMapper regionMapper;

    /**
     * 区域服务分页查询分页
     *
     * @param servePageQueryReqDTO
     * @return
     */
    @Override
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        PageResult<ServeResDTO> serveResDTOPageResult = PageHelperUtils.selectPage(servePageQueryReqDTO, () -> baseMapper.queryServeListByRegionId(servePageQueryReqDTO.getRegionId()));
        return serveResDTOPageResult;
    }


    /**
     * 批量新增区域服务
     *
     * @param serveUpsertReqDTOList
     */
    @Override
    public void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList) {
        for (ServeUpsertReqDTO serveUpsertReqDTO : serveUpsertReqDTOList) {
            //1 校验serve_item是否启用
            Long serveItemId = serveUpsertReqDTO.getServeItemId();
            ServeItem serveItem = serveItemMapper.selectById(serveItemId);
            if (ObjectUtil.isNull(serveItem) || serveItem.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
                //不能添加，抛出异常
                throw new ForbiddenOperationException("服务项不存在或未启用不允许添加");
            }
            //2 同一区域下不能添加相同的服务
            Integer count = lambdaQuery().eq(Serve::getServeItemId, serveUpsertReqDTO.getServeItemId())
                    .eq(Serve::getRegionId, serveUpsertReqDTO.getRegionId()).count();
            if (count > 0) {
                throw new ForbiddenOperationException(serveItem.getName() + "服务项已经存在");
            }
            //3 向serve表插入数据.
            Serve serve = BeanUtils.toBean(serveUpsertReqDTO, Serve.class);
            Long regionId = serve.getRegionId();
            Region region = regionMapper.selectById(regionId);
            serve.setCityCode(region.getCityCode());
            baseMapper.insert(serve);
        }
    }

    /**
     * 修改区域服务价格
     * @param id
     * @param price
     * @return
     */
    @Override
    public Serve update(Long id, BigDecimal price) {
        boolean update = lambdaUpdate().eq(Serve::getId, id).set(Serve::getPrice, price).update();
        if (!update) {
            throw new CommonException("修改失败");
        }
        Serve serve = baseMapper.selectById(id);
        return serve;
    }
}
