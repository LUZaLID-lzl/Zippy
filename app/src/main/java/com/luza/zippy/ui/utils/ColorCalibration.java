package com.luza.zippy.ui.utils;

import android.graphics.Color;
import android.util.Log;

public class ColorCalibration {
    private static final String TAG = "ColorCalibration";

    /**
     * 根据基准色计算渐变色系列
     * @param baseColor 基准色（十六进制颜色值，例如："#EAB1F4"）
     */
    public static void calculateGradientColors(String baseColor) {
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

    public static int[] judgeColor(String theme,int position){
        int[] startColors;
        int[] endColors;

        Log.d("ColorCalibration", "当前主题: " + theme + ", position: " + position);

        switch (theme) {
            case "pikachu":
                startColors = new int[]{
                        Color.parseColor("#FFC603"),
                        Color.parseColor("#FFE085"),
                        Color.parseColor("#FFD44C"),
                        Color.parseColor("#FFB300")
                };
                endColors = new int[]{
                        Color.parseColor("#FFE085"),
                        Color.parseColor("#FFD44C"),
                        Color.parseColor("#FFC603"),
                        Color.parseColor("#FFD44C")
                };
                break;
            case "bulbasaur":
                startColors = new int[]{
                        Color.parseColor("#13B4FC"),
                        Color.parseColor("#7ED8FA"),
                        Color.parseColor("#45C7FC"),
                        Color.parseColor("#0099E5")
                };
                endColors = new int[]{
                        Color.parseColor("#7ED8FA"),
                        Color.parseColor("#45C7FC"),
                        Color.parseColor("#13B4FC"),
                        Color.parseColor("#45C7FC")
                };
                break;
            case "squirtle":
                startColors = new int[]{
                        Color.parseColor("#5CB860"),
                        Color.parseColor("#96D897"),
                        Color.parseColor("#74C677"),
                        Color.parseColor("#45A948")
                };
                endColors = new int[]{
                        Color.parseColor("#96D897"),
                        Color.parseColor("#74C677"),
                        Color.parseColor("#5CB860"),
                        Color.parseColor("#74C677")
                };
                break;
            case "mew":
                startColors = new int[]{
                        Color.parseColor("#FBA7BD"),
                        Color.parseColor("#FDD3DE"),
                        Color.parseColor("#FCC0CE"),
                        Color.parseColor("#F98DA8")
                };
                endColors = new int[]{
                        Color.parseColor("#FDD3DE"),
                        Color.parseColor("#FCC0CE"),
                        Color.parseColor("#FBA7BD"),
                        Color.parseColor("#FCC0CE")
                };
                break;
            case "karsa":
                startColors = new int[]{
                        Color.parseColor("#EAB1F4"),  // 基准色
                        Color.parseColor("#F2CCF8"),  // 最浅色 (+15%)
                        Color.parseColor("#EEBEF6"),  // 中间偏浅 (+10%)
                        Color.parseColor("#E6A4F2")   // 中间偏深 (-10%)
                };
                endColors = new int[]{
                        Color.parseColor("#F2CCF8"),  // 最浅色
                        Color.parseColor("#EEBEF6"),  // 中间偏浅
                        Color.parseColor("#EAB1F4"),  // 基准色
                        Color.parseColor("#EEBEF6")   // 中间偏浅
                };
                break;
            case "capoo":
                startColors = new int[]{
                        Color.parseColor("#21FAD7"),  // 基准色
                        Color.parseColor("#25FFF7"),  // 最浅色 (+15%)
                        Color.parseColor("#24FFEC"),  // 中间偏浅 (+10%)
                        Color.parseColor("#1DE1C1")   // 中间偏深 (-10%)
                };
                endColors = new int[]{
                        Color.parseColor("#25FFF7"),  // 最浅色
                        Color.parseColor("#24FFEC"),  // 中间偏浅
                        Color.parseColor("#21FAD7"),  // 基准色
                        Color.parseColor("#24FFEC")   // 中间偏浅
                };
                break;
            default:
                startColors = new int[]{
                        Color.parseColor("#1A1A1A"),
                        Color.parseColor("#2D2D2D"),
                        Color.parseColor("#404040"),
                        Color.parseColor("#333333")
                };
                endColors = new int[]{
                        Color.parseColor("#2D2D2D"),
                        Color.parseColor("#404040"),
                        Color.parseColor("#1A1A1A"),
                        Color.parseColor("#404040")
                };
        }

        int[] result = position == 0 ? startColors : endColors;
        
        // 打印颜色值
        StringBuilder colorLog = new StringBuilder();
        colorLog.append("返回的颜色数组: [");
        for (int color : result) {
            colorLog.append(String.format("#%06X, ", (0xFFFFFF & color)));
        }
        colorLog.append("]");
        Log.d("ColorCalibration", colorLog.toString());

        return result;
    }
}
