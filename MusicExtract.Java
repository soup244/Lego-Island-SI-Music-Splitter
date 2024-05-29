package finalProject1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.*;

public class LegoIslandDechunk {

//This code was pure AGONY to write... If this code doesn't work, its eclipse and not me. (or maybe it's The Brickster)

	public static void main(String args[]) throws IOException {
//		// Window UI
		JFrame frame = new JFrame("Lego Island JUKEBOX.SI Extracter Tool");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JFileChooser fChoose = new JFileChooser();
		int returnVal = fChoose.showOpenDialog(frame);
		File jukeboxLocation = fChoose.getSelectedFile();

		// mainPanel.add(JBLocation,BorderLayout.WEST);

//
		System.out.println("You selected the directory: " + jukeboxLocation);

//
		// System.out.println(JukeboxFile);

		JLabel label = new JLabel();

		try {
			ImageIcon infoman = new ImageIcon(ImageIO.read(new File("infomaniac.png")));
			Image smallIcon = infoman.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
			label.setIcon(new ImageIcon(smallIcon));
		} catch (IOException e) {
			e.printStackTrace();

		}

		frame.add(label, BorderLayout.EAST);

		JRadioButton radButtonA = new JRadioButton("Dechunk Only");

		JRadioButton radButtonB = new JRadioButton("Read music names");
		JRadioButton radButtonC = new JRadioButton("Dechunk and spit to .WAV");
		ButtonGroup group = new ButtonGroup();
		group.add(radButtonA);
		group.add(radButtonB);
		group.add(radButtonC);
		radButtonA.setBounds(0, 0, 200, 30);
		radButtonB.setBounds(0, 0, 200, 30);
		radButtonC.setBounds(0, 0, 200, 30);

		frame.add(radButtonA);
		frame.add(radButtonB);
		frame.add(radButtonC);
		frame.setLayout(new GridLayout(0, 1));

		frame.setSize(390, 512);
		frame.setVisible(true);
		JButton button = new JButton("Run");
		button.setForeground(Color.BLACK);
		frame.add(button, BorderLayout.SOUTH);

//----------------------------------------------------------------------------------------------			

		button.addActionListener(e -> {
			if (radButtonA.isSelected()) {
				try { // DECHUNK

					dechunk(jukeboxLocation, 0);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if (radButtonB.isSelected()) {

//----------------------------------------------------------------------------------------------						

				try {//

					String namesOutput = "";
					ArrayList<String> nameTable = nameRip(jukeboxLocation, 1);
					for (int i = 0; i < nameTable.size(); i++) {

						namesOutput = namesOutput + (nameTable.get(i) + ", ");

						if (i % 5 == 0) {
							namesOutput = namesOutput + " \n";

						}

					}
					JOptionPane.showMessageDialog(frame, "DONE! Here are the names: \n\n" + namesOutput);

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if (radButtonC.isSelected()) {

//----------------------------------------------------------------------------------------------				

				try {

					writeWAV(jukeboxLocation);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// ----------------------------------------------------------------------------------------------

			} else {
				JOptionPane.showMessageDialog(frame, "No Option was Selected");
			}
		});

	}

	public static ArrayList<String> nameRip(File JUKEBOXinput, int display) throws IOException {

		ArrayList<String> names = new ArrayList<String>();

		File JUKEBOXSI = JUKEBOXinput;

		long totalBytes = JUKEBOXSI.length();
		byte[] MxChBytes = new byte[] { 0x4D, 0x78, 0x4F, 0x62 }; // MxCh
		FileInputStream jukeboxSource = new FileInputStream(JUKEBOXSI);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = jukeboxSource.read(buffer)) != -1) {
			for (int i = 0; i < bytesRead; i++) {

				if (i <= bytesRead - MxChBytes.length && buffer[i] == MxChBytes[0] && buffer[i + 1] == MxChBytes[1]
						&& buffer[i + 2] == MxChBytes[2] && buffer[i + 3] == MxChBytes[3]) {

					i += MxChBytes.length + 11;

					// find null byte
					StringBuilder sb = new StringBuilder();
					while (i < bytesRead && buffer[i] != 0) {
						sb.append((char) buffer[i]);
						i++;
					}
					String title = sb.toString();

					if (!title.contains("Movie") && !title.contains("Flic")) {

						names.add(title);

					}
				}
			}
		}
		if (display == 0) {
			System.out.println(names);
			return names;
		}

		return names;
	}

	public static void dechunk(File JUKEBOXinput, int view) throws IOException {
		String unchunkedFile = "MODIFIED.SI";
		byte[] MxChBytes = new byte[] { 0x4D, 0x78, 0x43, 0x68 };

		FileInputStream jukeboxSource = new FileInputStream(JUKEBOXinput);
		FileOutputStream jukeboxEdited = new FileOutputStream(unchunkedFile);

		byte[] buffer = new byte[1024];
		int bytesRead;
		boolean isChunk;
		long totalBytes = JUKEBOXinput.length();
		long progressBytes = 0;
		double percent;

		while ((bytesRead = jukeboxSource.read(buffer)) != -1) {
			for (int i = 0; i < bytesRead; i++) {
				isChunk = true;

				// Byte check
				for (int j = 0; j < MxChBytes.length; j++) {
					if (i + j >= bytesRead || buffer[i + j] != MxChBytes[j]) {
						isChunk = false;
						break;
					}
				}

				if (isChunk) {
					i += MxChBytes.length + 18 - 1;
				} else {
					jukeboxEdited.write(buffer[i]);
				}
			}
			progressBytes += bytesRead;
			percent = (double) progressBytes / totalBytes * 100;
			if (view == 1)
				System.out.println("Dechunking file... %" + percent);
			else
				System.out.println("Preparing to split... %" + percent);
		}
		System.out.println("Done");
	}

	public static ArrayList<byte[]> ripWAVHeaders(File JUKEBOXinput) throws IOException {
		byte[] chunkIdentifier = new byte[] { 0x4D, 0x78, 0x44, 0x61 };

		byte[] fileBytes = Files.readAllBytes(Paths.get(JUKEBOXinput.getPath()));

		ArrayList<byte[]> chunks = new ArrayList<>();

		for (int i = 0; i < fileBytes.length - chunkIdentifier.length - 27 - 19; i++) {
			boolean isChunk = true;

			for (int j = 0; j < chunkIdentifier.length; j++) {
				if (fileBytes[i + j] != chunkIdentifier[j]) {
					isChunk = false;
					break;
				}
			}
			if (isChunk) {
				byte[] chunk = new byte[19];
				System.arraycopy(fileBytes, i + chunkIdentifier.length + 27, chunk, 0, 19);
				chunks.add(chunk);
				System.out.println(Arrays.toString(chunk));
			}
		}
		System.out.println("Step 1 of 2 done");
		return chunks;
	}

	public static void writeWAV(File JUKEBOXinput) throws IOException {

		JFileChooser saver = new JFileChooser();
        saver.setDialogTitle("Specify a directory to save");
        saver.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int selection = saver.showSaveDialog(null);

        if (selection == JFileChooser.APPROVE_OPTION) {
            File saveDir = saver.getSelectedFile();
            
            
            
            
            
		dechunk(JUKEBOXinput, 1);
            
            
            
            
            
            
		ArrayList<byte[]> wavheaders = new ArrayList<byte[]>(ripWAVHeaders(JUKEBOXinput));
		ArrayList<String> names = new ArrayList<String>(nameRip(JUKEBOXinput, 0));

		byte[] headerTemplate = new byte[] { 0x52, 0x49, 0x46, 0x46, 0x60, 0x29, 0x2B, 0x00, 0x57, 0x41, 0x56, 0x45, 0x66, 0x6D, 0x74, 0x20, 0x18, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x11 };
		byte[] headerFooter = new byte[] { 0x64, 0x61, 0x74, 0x61, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

		byte[] targetBytes = new byte[] { 0x4D, 0x78, 0x44, 0x61 };
		byte[] buffer = new byte[1024];

		FileInputStream cleanSI = new FileInputStream("MODIFIED.SI");

		int bytesRead;
		boolean isChunk = false;
		boolean startWriting = false;

		for (int i = 0; i < names.size(); i++) {
			System.out.println("Writing " + names.get(i) + ".WAV...");
			File wavOut = new File(saveDir,names.get(i) + ".WAV");
			
			
			FileOutputStream output = new FileOutputStream(wavOut);

			output.write(headerTemplate);
			output.write(wavheaders.get(i));
			output.write(headerFooter);

			while ((bytesRead = cleanSI.read(buffer)) != -1) {
				for (int j = 0; j < bytesRead; j++) {
					isChunk = true;

					// Byte check
					for (int k = 0; k < targetBytes.length; k++) {
						if (j + k >= bytesRead || buffer[j + k] != targetBytes[k]) {
							isChunk = false;
							break;
						}
					}

					if (isChunk) {
						if (startWriting) {

							break;
						} else {
							startWriting = true;
							j += targetBytes.length - 1;
						}
					} else if (startWriting) {
						output.write(buffer[j]);
					}
				}
				if (isChunk && startWriting) {
					break;
				}
			}
		}

		System.out.println("Done");
	}

	}}
