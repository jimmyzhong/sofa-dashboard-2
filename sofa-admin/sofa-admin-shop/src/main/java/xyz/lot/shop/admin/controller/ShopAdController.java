package xyz.lot.shop.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import xyz.lot.common.annotation.AjaxWrapper;
import xyz.lot.common.domain.PageModel;
import xyz.lot.common.exception.BusinessException;
import xyz.lot.db.mongo.util.PageRequestUtil;
import xyz.lot.shop.admin.config.ShopPermissions;

@Controller
@RequestMapping("/ext/shop/ad")
public class ShopAdController {

	private String prefix = "ext/shop/ad";

	@GetMapping
	public String ad() {
		return prefix + "/ad";
	}

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @GetMapping("/edit/{adId}")
    public String edit(@PathVariable("adId") Long adId, ModelMap model) {
//		ShopAd shopAd = shopServiceReference.adService.find(adId);
//		if (shopAd == null) {
//			throw BusinessException.build(String.format("广告不存在%s", adId));
//		}
//		model.addAttribute("ad", shopAd);
		return prefix + "/edit";
    }

	@RequiresPermissions(ShopPermissions.Ad.EDIT)
	@GetMapping("/status/{adId}")
	@AjaxWrapper
	public void status(@PathVariable("adId") Long adId,
						 @RequestParam(value = "status", required = true) Integer status) {
//		ShopAd shopAd = shopServiceReference.adService.find(adId);
//		if (shopAd == null) {
//			throw BusinessException.build(String.format("广告不存在%s", adId));
//		}
//		shopAd.setStatus(status);
//		shopServiceReference.adService.edit(shopAd);
	}

}
