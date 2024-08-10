package com.jzo2o.foundations.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Resource
    private ServeMapper serveMapper;

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
     *
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

    /**
     * 区域服务的上架
     *
     * @param id
     * @return
     */
    @Override
    public Serve onSale(Long id) {
        //查询serve信息
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("不存在该区域服务");
        }
        //serve的sale_status是0或者1可以上架
        Integer status = serve.getSaleStatus();
        if (!(status == FoundationStatusEnum.INIT.getStatus() || status == FoundationStatusEnum.DISABLE.getStatus())) {
            throw new ForbiddenOperationException("区域服务的状态是草稿或者下架时方可上架");
        }
        //服务项未启用不能上架
        Long serveItemId = serve.getServeItemId();
        ServeItem serveItem = serveItemMapper.selectById(serveItemId);
        Integer activeStatus = serveItem.getActiveStatus();
        if (activeStatus != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("服务项未启用不能上架");
        }
        //更新sale_status
        boolean update = lambdaUpdate().eq(Serve::getId, id).set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus()).update();
        if (!update) {
            throw new CommonException("服务上架失败");
        }
        Serve selectById = baseMapper.selectById(id);
        return selectById;
    }

    /**
     * 区域服务的下架
     *
     * @param id
     * @return
     */
    @Override
    public Serve offSale(Long id) {
        //查询serve信息
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("不存在该区域服务");
        }
        //serve的sale_status是2可以下架
        Integer status = serve.getSaleStatus();
        if (status != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("区域服务的状态是上架时才能下架");
        }
        LambdaQueryWrapper<Serve> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Serve::getId, id);
        serve.setSaleStatus(FoundationStatusEnum.ENABLE.getStatus());
        serveMapper.update(serve, queryWrapper);
        Serve serve1 = baseMapper.selectById(id);
        return serve1;
    }

    /**
     * 区域服务删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        //查询serve信息
        Serve serve = baseMapper.selectById(id);
        if (ObjectUtil.isNull(serve)) {
            throw new ForbiddenOperationException("不存在该区域服务");
        }
        //serve的sale_status是0草稿可以删除
        Integer status = serve.getSaleStatus();
        if (status != FoundationStatusEnum.INIT.getStatus()) {
            throw new ForbiddenOperationException("区域服务的状态是上架不能删除");
        }
        LambdaQueryWrapper<Serve> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Serve::getId, id);
        serveMapper.delete(queryWrapper);
    }
}
