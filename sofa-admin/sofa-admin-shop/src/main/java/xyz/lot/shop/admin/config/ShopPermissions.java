package xyz.lot.shop.admin.config;

public class ShopPermissions {

    public static final String PERM_PREFIX = "ext:shop:";

    public static class Ad {
        public static final String PREFIX = PERM_PREFIX + "ad:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Article {
        public static final String PREFIX = PERM_PREFIX + "article:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Category {
        public static final String PREFIX = PERM_PREFIX + "category:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Goods {
        public static final String PREFIX = PERM_PREFIX + "goods:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Notice {
        public static final String PREFIX = PERM_PREFIX + "notice:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Order {
        public static final String PREFIX = PERM_PREFIX + "order:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Template {
        public static final String PREFIX = PERM_PREFIX + "template:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class User {
        public static final String PREFIX = PERM_PREFIX + "user:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
        public static final String RESET_PWD = PREFIX + "resetPwd";
        public static final String RESET_PAY_PWD = PREFIX + "resetPayPwd";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class VirtualUser {
        public static final String PREFIX = PERM_PREFIX + "virtual:user:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
        public static final String RESET_PWD = PREFIX + "resetPwd";
        public static final String RESET_PAY_PWD = PREFIX + "resetPayPwd";
        public static final String MONEY_VIEW = PREFIX + "money:view";
        public static final String MONEY_EDIT = PREFIX + "money:edit";
        public static final String EXPORT = PREFIX + "export";
    }

    public static class Version {
        public static final String PREFIX = PERM_PREFIX + "version:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Consignment {
        public static final String PREFIX = PERM_PREFIX + "consignment:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class ConsignmentRule {
        public static final String PREFIX = PERM_PREFIX + "consignmentRule:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Lots {
        public static final String PREFIX = PERM_PREFIX + "lots:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class LotsCategory {
        public static final String PREFIX = PERM_PREFIX + "lotsCategory:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class LotsItem {
        public static final String PREFIX = PERM_PREFIX + "lotsItem:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class LotsReturnMoney {
        public static final String PREFIX = PERM_PREFIX + "lotsReturnMoney:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class LotDividendMoney {
        public static final String PREFIX = PERM_PREFIX + "lotDividendMoney:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class LotRenewed {
        public static final String PREFIX = PERM_PREFIX + "lotRenewed:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class ScoreExchangeMoney {
        public static final String PREFIX = PERM_PREFIX + "scoreExchangeMoney:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class Supplier {
        public static final String PREFIX = PERM_PREFIX + "supplier:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class VipInfo {
        public static final String PREFIX = PERM_PREFIX + "vipInfo:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class LotParamInfo {
        public static final String PREFIX = PERM_PREFIX + "lotParamInfo:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String ADD = PREFIX + "add";
    }

    public static class PlatformParamConfig {
        public static final String PREFIX = PERM_PREFIX + "platformParamConfig:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String ADD = PREFIX + "add";
    }

    public static class WITHDRAWAL {
        public static final String PREFIX = PERM_PREFIX + "withdrawal:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String AUDIT = PREFIX + "audit";
        public static final String EXPORT = PREFIX + "export";
    }

    public class LotDeposits {
        public static final String PREFIX = PERM_PREFIX + "lotDeposits:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
    }

    public class LotPrices {
        public static final String PREFIX = PERM_PREFIX + "lotPrices:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
    }

    public class LotDeal {
        public static final String PREFIX = PERM_PREFIX + "lotDeal:";
        public static final String VIEW = PREFIX + "view";
    }

    public class LotCost {
        public static final String PREFIX = PERM_PREFIX + "lotCost:";
        public static final String VIEW = PREFIX + "view";
    }

    public class Feedback {
        public static final String PREFIX = PERM_PREFIX + "feedback:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
    }

    public class Agents {
        public static final String PREFIX = PERM_PREFIX + "agents:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
    }

    public class TopUp {
        public static final String PREFIX = PERM_PREFIX + "topUp:";
        public static final String VIEW = PREFIX + "view";
        public static final String EXPORT = PREFIX + "export";
    }

    public class UserVip {
        public static final String PREFIX = PERM_PREFIX + "userVip:";
        public static final String VIEW = PREFIX + "view";
    }

    public class VipTxnLog {
        public static final String PREFIX = PERM_PREFIX + "vipTxnLog:";
        public static final String VIEW = PREFIX + "view";
    }
    public static class Agenter {
        public static final String PREFIX = PERM_PREFIX + "agenter:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
        public static final String STATS = PREFIX + "stats";
    }

    public class ItemHistory {
        public static final String PREFIX = PERM_PREFIX + "itemHistory:";
        public static final String VIEW = PREFIX + "view";
    }

    public class ServiceCharge {
        public static final String PREFIX = PERM_PREFIX + "serviceCharge:";
        public static final String VIEW = PREFIX + "view";
        public static final String ADD = PREFIX + "add";
    }

    public static class CurrencyConfig {
        public static final String PREFIX = PERM_PREFIX + "currencyConfig:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class LevelDivisionConfig {
        public static final String PREFIX = PERM_PREFIX + "levelDivisionConfig:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class WithdrawConfig {
        public static final String PREFIX = PERM_PREFIX + "withdrawConfig:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class RankDividendConfig {
        public static final String PREFIX = PERM_PREFIX + "rankDividendConfig:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class UserLevelWhite {
        public static final String PREFIX = PERM_PREFIX + "userLevelWhite:";
        public static final String VIEW = PREFIX + "view";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }

    public static class WeixinConfig {
        public static final String PREFIX = PERM_PREFIX + "weixinConfig:";
        public static final String VIEW = PREFIX + "view";
        public static final String EDIT = PREFIX + "edit";
        public static final String REMOVE = PREFIX + "remove";
        public static final String ADD = PREFIX + "add";
    }
}
