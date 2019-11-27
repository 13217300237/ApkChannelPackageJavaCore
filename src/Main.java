
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
			System.out.println("=======ȱ�ٲ���mode======");
		}
		switch (mode.trim()) {
		case "1":
			// ���Ե������
			File baseV2Apk = new File("./origin/app-debug_v1.apk");
			String channel = "xiaomi-black";
			try {
				test(baseV2Apk, channel);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "2":
			// ���Զ�����
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
			System.out.println("=======modeֻ������1��2======");
			break;
		}

	}

	private static void test(File baseApk, String channel) throws Exception {
		ChannelHelper.reset();
		System.out.println("��Ҫס���������Ϣ��:" + channel);
		long l = System.currentTimeMillis();
		File outDir = new File("./output");// �������Ŀ¼
		outDir.mkdirs();// �������Ŀ¼
		String name = baseApk.getName();
		name = name.substring(0, name.lastIndexOf("."));
		Apk apk = ApkParser.parser(baseApk);// ��apk�ļ������������Զ����Apk��Ķ���
		File file = new File(outDir, name + "-" + channel + ".apk");// ����APK
		ApkBuilder.generateChannel(channel, apk, file);
		System.out.println("ע��������Ϣ ��ʱ��" + (System.currentTimeMillis() - l) + " MS");
		System.out.println("======================");
		System.out.println("���������Ϣ:" + ChannelHelper.getChannel(file.getAbsolutePath()));
	}

	private static void testMultiPackage(ChannelExt channelExt) throws Exception {
		System.out.println("==============��������������߼�===============" + channelExt);
		// ��ȡ���úõĲ���
		if (channelExt == null || !channelExt.isOk()) {
			System.out.println(" û��ȡ�ñ���Ĳ���...");
			return;
		}
		File baseFile = new File(channelExt.getBaseApkPath());
		File channelConfigFile = new File(channelExt.getChannelConfigPath());
		File outDirFile = new File(channelExt.getOutDir());
		File themeConfigFile = new File(channelExt.getThemeConfigPath());
		outDirFile.mkdirs();
		List<String> channelConfigs = FlavorUtil.getStrListFromFile(channelConfigFile);
		List<String> themeConfigs = FlavorUtil.getStrListFromFile(themeConfigFile);
		// Ȼ����������list�ĳ˻�(����A��4��Ԫ�أ�����B��5��Ԫ�أ����Գ˻�һ����20��Ԫ��)
		List<String> finalFlavors = FlavorUtil.calculateListProduct(channelConfigs, themeConfigs);
		for (String flavorName : finalFlavors) {
			Apk apk = ApkParser.parser(baseFile);// 2������APK(zip�ļ�)
			File file = new File(outDirFile, "app-debug-" + flavorName + ".apk");
			ApkBuilder.generateChannel(flavorName, apk, file);// 3������APK
		}
	}

}
