package xyz.lot.dashboard.web.demo.controller;

import xyz.lot.dashboard.common.util.StringUtil;
import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.common.domain.PageModel;
import xyz.lot.common.domain.PageRequest;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.common.util.Convert;
import xyz.lot.dashboard.web.demo.domain.UserOperateModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作控制
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/demo/operate")
public class DemoOperateController {
    private String prefix = "demo/operate";

    private final static Map<Integer, UserOperateModel> users = new LinkedHashMap<Integer, UserOperateModel>();

    {
        users.put(1, new UserOperateModel(1, "1000001", "测试1", "0", "15888888888", "ry@qq.com", 150.0, "0"));
        users.put(2, new UserOperateModel(2, "1000002", "测试2", "1", "15666666666", "ry@qq.com", 180.0, "1"));
        users.put(3, new UserOperateModel(3, "1000003", "测试3", "0", "15666666666", "ry@qq.com", 110.0, "1"));
        users.put(4, new UserOperateModel(4, "1000004", "测试4", "1", "15666666666", "ry@qq.com", 220.0, "1"));
        users.put(5, new UserOperateModel(5, "1000005", "测试5", "0", "15666666666", "ry@qq.com", 140.0, "1"));
        users.put(6, new UserOperateModel(6, "1000006", "测试6", "1", "15666666666", "ry@qq.com", 330.0, "1"));
        users.put(7, new UserOperateModel(7, "1000007", "测试7", "0", "15666666666", "ry@qq.com", 160.0, "1"));
        users.put(8, new UserOperateModel(8, "1000008", "测试8", "1", "15666666666", "ry@qq.com", 170.0, "1"));
        users.put(9, new UserOperateModel(9, "1000009", "测试9", "0", "15666666666", "ry@qq.com", 180.0, "1"));
        users.put(10, new UserOperateModel(10, "1000010", "测试10", "0", "15666666666", "ry@qq.com", 210.0, "1"));
        users.put(11, new UserOperateModel(11, "1000011", "测试11", "1", "15666666666", "ry@qq.com", 110.0, "1"));
        users.put(12, new UserOperateModel(12, "1000012", "测试12", "0", "15666666666", "ry@qq.com", 120.0, "1"));
        users.put(13, new UserOperateModel(13, "1000013", "测试13", "1", "15666666666", "ry@qq.com", 380.0, "1"));
        users.put(14, new UserOperateModel(14, "1000014", "测试14", "0", "15666666666", "ry@qq.com", 280.0, "1"));
        users.put(15, new UserOperateModel(15, "1000015", "测试15", "0", "15666666666", "ry@qq.com", 570.0, "1"));
        users.put(16, new UserOperateModel(16, "1000016", "测试16", "1", "15666666666", "ry@qq.com", 260.0, "1"));
        users.put(17, new UserOperateModel(17, "1000017", "测试17", "1", "15666666666", "ry@qq.com", 210.0, "1"));
        users.put(18, new UserOperateModel(18, "1000018", "测试18", "1", "15666666666", "ry@qq.com", 340.0, "1"));
        users.put(19, new UserOperateModel(19, "1000019", "测试19", "1", "15666666666", "ry@qq.com", 160.0, "1"));
        users.put(20, new UserOperateModel(20, "1000020", "测试20", "1", "15666666666", "ry@qq.com", 220.0, "1"));
        users.put(21, new UserOperateModel(21, "1000021", "测试21", "1", "15666666666", "ry@qq.com", 120.0, "1"));
        users.put(22, new UserOperateModel(22, "1000022", "测试22", "1", "15666666666", "ry@qq.com", 130.0, "1"));
        users.put(23, new UserOperateModel(23, "1000023", "测试23", "1", "15666666666", "ry@qq.com", 490.0, "1"));
        users.put(24, new UserOperateModel(24, "1000024", "测试24", "1", "15666666666", "ry@qq.com", 570.0, "1"));
        users.put(25, new UserOperateModel(25, "1000025", "测试25", "1", "15666666666", "ry@qq.com", 250.0, "1"));
        users.put(26, new UserOperateModel(26, "1000026", "测试26", "1", "15666666666", "ry@qq.com", 250.0, "1"));
    }

    /**
     * 表格
     */
    @GetMapping("/table")
    public String table() {
        return prefix + "/table";
    }

    /**
     * 其他
     */
    @GetMapping("/other")
    public String other() {
        return prefix + "/other";
    }

    /**
     * 查询数据
     */
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel list(HttpServletRequest request, UserOperateModel userModel) {
        PageModel rspData = new PageModel();
        List<UserOperateModel> userList = new ArrayList<UserOperateModel>(users.values());
        // 查询条件过滤

        if (userModel.getSearchValue() != null) {
            userList.clear();
            for (Map.Entry<Integer, UserOperateModel> entry : users.entrySet()) {
                if (entry.getValue().getUserName().equals(userModel.getSearchValue())) {
                    userList.add(entry.getValue());
                }
            }
        }
        else if (StringUtil.isNotEmpty(userModel.getUserName()))
        {
            userList.clear();
            for (Map.Entry<Integer, UserOperateModel> entry : users.entrySet())
            {
                if (entry.getValue().getUserName().equals(userModel.getUserName()))
                {
                    userList.add(entry.getValue());
                }
            }
        }
        PageRequest pageDomain = PageRequestUtil.fromRequest(request);
        if (pageDomain.getPageNum() < 0 || pageDomain.getPageSize() < 0) {
            rspData.setRows(userList);
            rspData.setCount((long) userList.size());
            return rspData;
        }
        long pageNum = (pageDomain.getPageNum() - 1) * 10;
        long pageSize = pageDomain.getPageNum() * 10;
        if (pageSize > userList.size()) {
            pageSize = userList.size();
        }
        rspData.setRows(userList.subList((int) pageNum, (int) pageSize));
        rspData.setCount((long) userList.size());
        return rspData;
    }

    /**
     * 新增用户
     */
    @GetMapping("/add")
    public String add(ModelMap mmap) {
        return prefix + "/add";
    }

    /**
     * 新增保存用户
     */
    @PostMapping("/add")
    @ResponseBody
    @AjaxWrapper
    public UserOperateModel addSave(UserOperateModel user) {
        Integer userId = users.size() + 1;
        user.setUserId(userId);
        return users.put(userId, user);
    }

    /**
     * 修改用户
     */
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Integer userId, ModelMap mmap) {
        mmap.put("user", users.get(userId));
        return prefix + "/edit";
    }

    /**
     * 修改保存用户
     */
    @PostMapping("/edit")
    @ResponseBody
    @AjaxWrapper
    public UserOperateModel editSave(UserOperateModel user) {
        return users.put(user.getUserId(), user);
    }

    /**
     * 导出
     */
//    @PostMapping("/export")
//    @ResponseBody
//    @AjaxWrapper
//    public UserOperateModel export(UserOperateModel user)
//    {
//        List<UserOperateModel> list = new ArrayList<UserOperateModel>(users.values());
//        ExcelUtil<UserOperateModel> util = new ExcelUtil<UserOperateModel>(UserOperateModel.class);
//        return util.exportExcel(list, "用户数据");
//    }

    /**
     * 下载模板
     */
//    @GetMapping("/importTemplate")
//    @ResponseBody
//    public AjaxResult importTemplate()
//    {
//        ExcelUtil<UserOperateModel> util = new ExcelUtil<UserOperateModel>(UserOperateModel.class);
//        return util.importTemplateExcel("用户数据");
//    }

    /**
     * 导入数据
     */
//    @PostMapping("/importData")
//    @ResponseBody
//    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
//    {
//        ExcelUtil<UserOperateModel> util = new ExcelUtil<UserOperateModel>(UserOperateModel.class);
//        List<UserOperateModel> userList = util.importExcel(file.getInputStream());
//        String message = importUser(userList, updateSupport);
//        return AjaxResult.success(message);
//    }

    /**
     * 删除用户
     */
    @PostMapping("/remove")
    @ResponseBody
    @AjaxWrapper
    public boolean remove(String ids) {
        Integer[] userIds = Convert.toIntArray(ids);
        for (Integer userId : userIds) {
            users.remove(userId);
        }
        return true;
    }

    /**
     * 查看详细
     */
    @GetMapping("/detail/{userId}")
    public String detail(@PathVariable("userId") Integer userId, ModelMap mmap) {
        mmap.put("user", users.get(userId));
        return prefix + "/detail";
    }

    @PostMapping("/clean")
    @ResponseBody
    @AjaxWrapper
    public boolean clean() {
        users.clear();
        return true;
    }

    /**
     * 导入用户数据
     *
     * @param userList        用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 结果
     */
    public String importUser(List<UserOperateModel> userList, Boolean isUpdateSupport) {
        if (userList == null || userList.size() == 0) {
            throw BusinessException.build("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (UserOperateModel user : userList) {
            try {
                // 验证是否存在这个用户
                boolean userFlag = false;
                for (Map.Entry<Integer, UserOperateModel> entry : users.entrySet()) {
                    if (entry.getValue().getUserName().equals(user.getUserName())) {
                        userFlag = true;
                        break;
                    }
                }
                if (!userFlag) {
                    Integer userId = users.size() + 1;
                    user.setUserId(userId);
                    users.put(userId, user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、用户 " + user.getUserName() + " 导入成功");
                } else if (isUpdateSupport) {
                    users.put(user.getUserId(), user);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、用户 " + user.getUserName() + " 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、用户 " + user.getUserName() + " 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw BusinessException.build(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}
