
import java.io.File;
import java.util.List;

import com.zhou.channellib.core.ChannelExt;
import com.zhou.channellib.core.FlavorUtil;

import channel.ApkBuilder;
import channel.ApkParser;
import channel.ChannelHelper;
import channel.data.Apk;

public class Main {

	public static void main(String[] args) {
		String mode = null;
		if (args != null && args.length > 0) {
			mode = args[0];
		}
		if (mode == null) {
			System.out.println("=======缺少参数mode======");
		}
		switch (mode.trim()) {
		case "1":
			// 尝试单个打包
			File baseV2Apk = new File("./origin/app-debug_v1.apk");
			String channel = "xiaomi-black";
			try {
				test(baseV2Apk, channel);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "2":
			// 尝试多个打包
			ChannelExt ext = new ChannelExt();
			ext.setBaseApkPath("./origin/app-debug_v2.apk");
			ext.setChannelConfigPath("./flavorConfig/channel.txt");
			ext.setThemeConfigPath("./flavorConfig/theme.txt");
			ext.setOutDir("./output");
			try {
				testMultiPackage(ext);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("=======mode只可以是1和2======");
			break;
		}

	}

	private static void test(File baseApk, String channel) throws Exception {
		ChannelHelper.reset();
		System.out.println("需要住入的渠道信息是:" + channel);
		long l = System.currentTimeMillis();
		File outDir = new File("./output");// 创建输出目录
		outDir.mkdirs();// 生成输出目录
		String name = baseApk.getName();
		name = name.substring(0, name.lastIndexOf("."));
		Apk apk = ApkParser.parser(baseApk);// 把apk文件解析成我们自定义的Apk类的对象
		File file = new File(outDir, name + "-" + channel + ".apk");// 生成APK
		ApkBuilder.generateChannel(channel, apk, file);
		System.out.println("注入渠道信息 耗时：" + (System.currentTimeMillis() - l) + " MS");
		System.out.println("======================");
		System.out.println("解读渠道信息:" + ChannelHelper.getChannel(file.getAbsolutePath()));
	}

	private static void testMultiPackage(ChannelExt channelExt) throws Exception {
		System.out.println("==============进入渠道包打包逻辑===============" + channelExt);
		// 读取配置好的参数
		if (channelExt == null || !channelExt.isOk()) {
			System.out.println(" 没有取得必须的参数...");
			return;
		}
		File baseFile = new File(channelExt.getBaseApkPath());
		File channelConfigFile = new File(channelExt.getChannelConfigPath());
		File outDirFile = new File(channelExt.getOutDir());
		File themeConfigFile = new File(channelExt.getThemeConfigPath());
		outDirFile.mkdirs();
		List<String> channelConfigs = FlavorUtil.getStrListFromFile(channelConfigFile);
		List<String> themeConfigs = FlavorUtil.getStrListFromFile(themeConfigFile);
		// 然后计算出两个list的乘积(数组A有4个元素，数组B有5个元素，所以乘积一共有20个元素)
		List<String> finalFlavors = FlavorUtil.calculateListProduct(channelConfigs, themeConfigs);
		for (String flavorName : finalFlavors) {
			Apk apk = ApkParser.parser(baseFile);// 2、解析APK(zip文件)
			File file = new File(outDirFile, "app-debug-" + flavorName + ".apk");
			ApkBuilder.generateChannel(flavorName, apk, file);// 3、生成APK
		}
	}

}
