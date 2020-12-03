package cn.edu.fudan.issueservice.domain.enums;

/**
 * @author Beethoven
 */

public enum IssueTypeInChineseEnum {
    /**
     * 规则中英文对照
     */
    rule1("\".equals()\" should not be used to test the values of \"Atomic\" classes", "不要使用equals方法对Atomic类进行是否相等的判断"),
    rule2("\"@Deprecated\" code should not be used", "不应该在程序中使用 @Deprecated 标注的接口、类和方法"),
    rule3("\"BigDecimal(double)\" should not be used", "不要使用BigDecimal(double)去构造一个BigDecimal对象"),
    rule4("\"clone\" should not be overridden", "不应重写clone方法"),
    rule5("\"equals(Object obj)\" and \"hashCode()\" should be overridden in pairs", "equals(Object obj)和hashCode()应该同时重写"),
    rule6("\"Iterator.next()\" methods should throw \"NoSuchElementException\"","Iterator.next()应该抛出NoSuchElementException异常"),
    rule7("\"notifyAll\" should be used", "此处应该使用notifyAll而不是notify"),
    rule8("\"null\" should not be used with \"Optional\"", "null不应与Optional一起使用"),
    rule9("\"NullPointerException\" should not be caught", "空指针不应捕获处理"),
    rule10("\"NullPointerException\" should not be explicitly thrown", "NullPointerException不应该被显式抛出"),
    rule11("\"Thread.run()\" should not be called directly", "不应该直接调用Thread对象的run方法"),
    rule12("\"wait\" should not be called when multiple locks are held", "当有多个锁时，不应调用“wait”"),
    rule13("[p3c]Do not add 'is' as prefix while defining Boolean variable.", "[p3c]定义布尔变量时不要添加“is”作为前缀"),
    rule14("[p3c]Do not remove or add elements to a collection in a foreach loop.", "[p3c]不要在foreach循环中删除或向集合添加元素"),
    rule15("[p3c]Do not use methods which will modify the list after using Arrays.asList to convert array to list.", "[p3c]不要使用在使用后会修改列表的方法数组Arrays.asList"),
    rule16("[p3c]Manually create thread pool is better.", "[p3c]最好手动创建线程池"),
    rule17("[p3c]SimpleDataFormat is unsafe, do not define it as a static variable. If have to, lock or DateUtils class must be used.", "[p3c]SimpleDataFormat不安全，不要将其定义为静态变量。如果必须定义，则必须使用lock或DateUtils类"),
    rule18("Boxing and unboxing should not be immediately reversed", "装箱（创建int/Integer类型值的对象）和拆箱（将对象中原始值解出来）不应连续操作"),
    rule19("Collection sizes and array length comparisons should make sense", "集合大小和数组长度比较应该是有意义的"),
    rule20("Conditionally executed code should be reachable", "条件执行块应该是可访问的"),
    rule21("Empty arrays and collections should be returned instead of null", "应该返回空数组和集合，而不是null"),
    rule22("Instance methods should not write to \"static\" fields", "实例方法不应写入静态字段"),
    rule23("Locks should be released", "保证锁能够释放"),
    rule24("Loops should not be infinite", "循环不应该是无限的"),
    rule25("Math operands should be cast before assignment", "赋值前应转换数学操作数"),
    rule26("Related \"if/else if\" statements should not have the same condition", "if语句不应该有相同的条件"),
    rule27("Resources should be closed", "资源应该关闭"),
    rule28("String literals should not be duplicated", "字符串文字不应重复"),
    rule29("Strings and Boxed types should be compared using \"equals()\"", "字符串和包装类型对比时应该使用equals方法"),
    rule30("Try-with-resources should be used", "应该使用Try-with-resources"),
    rule31("Week Year (\"YYYY\") should not be used for date formatting", "星期 年（“YYYY”）不应用于日期格式"),
    rule32("Zero should not be a possible denominator", "零不应该是一个分母");

    private final String name;
    private final String nameInChinese;

    IssueTypeInChineseEnum(String name, String nameInChinese) {
        this.name = name;
        this.nameInChinese = nameInChinese;
    }

    public static String getIssueTypeInChinese(String issueType){
        for(IssueTypeInChineseEnum issueTypeInChineseEnum : IssueTypeInChineseEnum.values()){
            if(issueTypeInChineseEnum.name.equals(issueType)){
                return issueTypeInChineseEnum.nameInChinese;
            }
        }
        return null;
    }

}