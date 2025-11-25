package com.luza.zippy.ui.utils;

import android.graphics.Color;
import android.util.Log;

import com.luza.zippy.MainActivity;
import com.luza.zippy.setting.ShardPerfenceSetting;

public class ColorCalibration {
    private static final String TAG = "ColorCalibration";

    // 静态变量存储实际显示的颜色
    private static String actualBottomColor = "#FFFFFF";
    private static String actualTopColor = "#FFFFFF";

    /**
     * 设置实际显示的颜色
     * @param bottomColor 实际底部颜色
     * @param topColor 实际顶部颜色
     */
    public static void setActualDisplayedColors(String bottomColor, String topColor) {
        actualBottomColor = bottomColor;
        actualTopColor = topColor;
        Log.d(TAG, "设置实际显示颜色 - 底部: " + bottomColor + ", 顶部: " + topColor);
    }

    /**
     * 获取实际底部颜色
     * @return 实际底部颜色
     */
    public static String getActualBottomColor() {
        return actualBottomColor;
    }

    /**
     * 获取实际顶部颜色
     * @return 实际顶部颜色
     */
    public static String getActualTopColor() {
        return actualTopColor;
    }

    /**
     * 根据基准色计算渐变色系列
     */
    public static void calculateGradientColors() {
        String baseColor = getThemeColor(ShardPerfenceSetting.getInstance(MainActivity.mContext).getHomeTheme());
        try {
            // 解析基准色
            int color = Color.parseColor(baseColor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);

            // 计算最浅色 (+15% 亮度)
            int lightestRed = Math.min(255, (int)(red * 1.15));
            int lightestGreen = Math.min(255, (int)(green * 1.15));
            int lightestBlue = Math.min(255, (int)(blue * 1.15));
            String lightestColor = String.format("#%02X%02X%02X", lightestRed, lightestGreen, lightestBlue);

            // 计算中间偏浅色 (+10% 亮度)
            int lighterRed = Math.min(255, (int)(red * 1.10));
            int lighterGreen = Math.min(255, (int)(green * 1.10));
            int lighterBlue = Math.min(255, (int)(blue * 1.10));
            String lighterColor = String.format("#%02X%02X%02X", lighterRed, lighterGreen, lighterBlue);

            // 计算中间偏深色 (-10% 亮度)
            int darkerRed = (int)(red * 0.90);
            int darkerGreen = (int)(green * 0.90);
            int darkerBlue = (int)(blue * 0.90);
            String darkerColor = String.format("#%02X%02X%02X", darkerRed, darkerGreen, darkerBlue);

            // 打印颜色值
            Log.d(TAG, "\n颜色系列（基准色：" + baseColor + "）：");
            Log.d(TAG, "最浅色：" + lightestColor + " (+15%)");
            Log.d(TAG, "中间偏浅：" + lighterColor + " (+10%)");
            Log.d(TAG, "基准色：" + baseColor);
            Log.d(TAG, "中间偏深：" + darkerColor + " (-10%)");

            // 打印渐变色数组配置
            Log.d(TAG, "\nstartColors 配置：");
            Log.d(TAG, "startColors = new int[]{\n" +
                    "    Color.parseColor(\"" + baseColor + "\"),  // 基准色\n" +
                    "    Color.parseColor(\"" + lightestColor + "\"),  // 最浅色\n" +
                    "    Color.parseColor(\"" + lighterColor + "\"),  // 中间偏浅\n" +
                    "    Color.parseColor(\"" + darkerColor + "\")   // 中间偏深\n" +
                    "};");

            Log.d(TAG, "\nendColors 配置：");
            Log.d(TAG, "endColors = new int[]{\n" +
                    "    Color.parseColor(\"" + lightestColor + "\"),  // 最浅色\n" +
                    "    Color.parseColor(\"" + lighterColor + "\"),  // 中间偏浅\n" +
                    "    Color.parseColor(\"" + baseColor + "\"),  // 基准色\n" +
                    "    Color.parseColor(\"" + lighterColor + "\")   // 中间偏浅\n" +
                    "};");

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "颜色格式错误：" + baseColor);
        }
    }

    public static String getThemeColor(String theme) {
        String themeColor;
        switch (theme) {
            case "pikachu":
                themeColor = "#FFC603";  // 皮卡丘黄色
                break;
            case "bulbasaur":
                themeColor = "#13B4FC";  // 妙蛙种子蓝色
                break;
            case "squirtle":
                themeColor = "#5CB860";  // 杰尼龟绿色
                break;
            case "mew":
                themeColor = "#FBA7BD";  // 梦幻粉色
                break;
            case "karsa":
                themeColor = "#EAB1F4";  // karsa紫色
                break;
            case "capoo":
                themeColor = "#21FAD7";  // capoo青色
                break;
            case "maple":
                themeColor = "#D8420C";  // maple橙色
                break;
            case "winter":
                themeColor = "#C6DEDD";  // winter淡灰色
                break;
            case "gengar":
                themeColor = "#200225";  // gengar紫色
                break;
            default:
                themeColor = "#1A1A1A";  // 默认深灰色
        }
        //calculateGradientColors(themeColor);
        return themeColor;
    }

    public static String getThemeColorType(String theme,int type){
        /***
         * type
         * 1: 基准色
         * 2：底部框背景颜色（基于实际底部颜色，模拟渐变叠加效果）
         * 3：单个BLOCK背景颜色（实际底部颜色）
         * 4：收藏BLOCK背景颜色（基于实际底部颜色，模拟渐变叠加效果）
         */
        String returnColor = "FFFFFF";
        
        switch (type){
            case 1:
                returnColor = getThemeColor(theme);
                break;
            case 2:
                // 模拟渐变叠加效果 - 更亮的叠加
                returnColor = simulateGradientOverlay(getActualBottomColor(), 0.4f);
                break;
            case 3:
                // 返回实际底部颜色（考虑动画和叠加效果）
                returnColor = getActualBottomColor();
                break;
            case 4:
                // 模拟渐变叠加效果 - 更暗的叠加
                returnColor = simulateGradientOverlay(getActualBottomColor(), 0.2f);
                break;
            default:
                returnColor = getThemeColor(theme);
                break;
        }

        Log.d(TAG, "type：" + type + "   returnColor:" + returnColor);
        return returnColor;
    }

    /**
     * 模拟渐变叠加效果（类似 LiquidBackgroundView 中的白色叠加）
     * @param colorHex 十六进制颜色值（如 "#FFC603"）
     * @param overlayIntensity 叠加强度（0.0-1.0，值越大越亮）
     * @return 调整后的十六进制颜色值
     */
    private static String simulateGradientOverlay(String colorHex, float overlayIntensity) {
        try {
            int color = Color.parseColor(colorHex);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            
            // 模拟白色叠加效果（类似 LiquidBackgroundView 中的效果）
            // overlayIntensity 控制叠加的强度
            red = Math.min(255, red + (int)((255 - red) * overlayIntensity));
            green = Math.min(255, green + (int)((255 - green) * overlayIntensity));
            blue = Math.min(255, blue + (int)((255 - blue) * overlayIntensity));
            
            return String.format("#%02X%02X%02X", red, green, blue);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "颜色格式错误：" + colorHex);
            return colorHex; // 返回原颜色
        }
    }

    public static int[] judgeColor(String theme, int position) {
        String baseColor;

        baseColor = getThemeColor(theme);

        // 解析基准色
        int color = Color.parseColor(baseColor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        // 计算渐变色系列
        int[] startColors = new int[4];
        int[] endColors = new int[4];

        // 计算各个亮度的颜色
        int[] lightestRGB = calculateAdjustedColor(red, green, blue, 1.15f);  // +15%
        int[] lighterRGB = calculateAdjustedColor(red, green, blue, 1.10f);   // +10%
        int[] darkerRGB = calculateAdjustedColor(red, green, blue, 0.90f);    // -10%

        // 设置startColors
        startColors[0] = color;  // 基准色
        startColors[1] = Color.rgb(lightestRGB[0], lightestRGB[1], lightestRGB[2]);  // 最浅色
        startColors[2] = Color.rgb(lighterRGB[0], lighterRGB[1], lighterRGB[2]);     // 中间偏浅
        startColors[3] = Color.rgb(darkerRGB[0], darkerRGB[1], darkerRGB[2]);       // 中间偏深

        // 设置endColors
        endColors[0] = startColors[1];  // 最浅色
        endColors[1] = startColors[2];  // 中间偏浅
        endColors[2] = startColors[0];  // 基准色
        endColors[3] = startColors[2];  // 中间偏浅

        // 打印日志
        logColorArray(theme, position, position == 0 ? startColors : endColors);

        return position == 0 ? startColors : endColors;
    }

    // 新增：计算调整后的RGB颜色值
    private static int[] calculateAdjustedColor(int red, int green, int blue, float factor) {
        return new int[]{
            Math.min(255, (int)(red * factor)),
            Math.min(255, (int)(green * factor)),
            Math.min(255, (int)(blue * factor))
        };
    }

    // 新增：打印颜色数组日志
    private static void logColorArray(String theme, int position, int[] colors) {
        StringBuilder colorLog = new StringBuilder();
        colorLog.append("主题: ").append(theme)
               .append(", position: ").append(position)
               .append(", 颜色数组: [");
        for (int color : colors) {
            colorLog.append(String.format("#%06X, ", (0xFFFFFF & color)));
        }
        colorLog.append("]");
        Log.d("ColorCalibration", colorLog.toString());
    }
}
