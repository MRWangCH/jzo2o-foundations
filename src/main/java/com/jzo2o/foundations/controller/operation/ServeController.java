package com.jzo2o.foundations.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: wangchuan
 * @Desc: 区域服务管理相关接口
 * @create: 2024-07-10 21:04
 **/

@RestController("operationServeController")
@RequestMapping("/operation/serve")
@Api(tags = "运营端-区域服务管理相关的接口")
public class ServeController {

    @Resource
    private IServeService serveService;

    @ApiOperation("区域服务分页查询")
    @GetMapping("/page")
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        return serveService.page(servePageQueryReqDTO);
    }

    @ApiOperation("添加区域服务")
    @PostMapping("/batch")
    public void add(@RequestBody List<ServeUpsertReqDTO> serveUpsertReqDTOList) {
        serveService.batchAdd(serveUpsertReqDTOList);
    }

    @ApiOperation("修改区域服务价格")
    @PutMapping("/{id}")
    public void update(@PathVariable("id") Long id, BigDecimal price) {
        serveService.update(id, price);
    }

    @ApiOperation("区域服务上架")
    @PutMapping("/onSale/{id}")
    public void onSale(@PathVariable("id") Long id) {
        serveService.onSale(id);
    }

}
