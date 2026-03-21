/*
 * Sokoban - Encore une nouvelle version (à but pédagogique) du célèbre jeu
 * Copyright (C) 2018 Guillaume Huard
 *
 * Ce programme est libre, vous pouvez le redistribuer et/ou le
 * modifier selon les termes de la Licence Publique Générale GNU publiée par la
 * Free Software Foundation (version 2 ou bien toute autre version ultérieure
 * choisie par vous).
 *
 * Ce programme est distribué car potentiellement utile, mais SANS
 * AUCUNE GARANTIE, ni explicite ni implicite, y compris les garanties de
 * commercialisation ou d'adaptation dans un but spécifique. Reportez-vous à la
 * Licence Publique Générale GNU pour plus de détails.
 *
 * Vous devez avoir reçu une copie de la Licence Publique Générale
 * GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
 * États-Unis.
 *
 * Contact:
 *          Guillaume.Huard@imag.fr
 *          Laboratoire LIG
 *          700 avenue centrale
 *          Domaine universitaire
 *          38401 Saint Martin d'Hères
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.FileInputStream;

class AireDeDessin extends JComponent {
	int counter;
	Image img;
	Point center;


	public AireDeDessin() {
		// Chargement de l'image de la même manière que le fichier de niveaux
		try {
			InputStream in = new FileInputStream("Images/cow.png");
			// Chargement d'une image utilisable dans Swing
			img = ImageIO.read(in);
		} catch (Exception e) {
			System.err.println("Impossible de charger l'image");
			System.exit(1);
		}
		counter = 1;
	}


	protected void deplace(int x, int y) {
		center = new Point(x, y);
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		System.out.println("Entree dans paintComponent : " + counter++);

		// Graphics 2D est le vrai type de l'objet passé en paramètre
		// Le cast permet d'avoir acces a un peu plus de primitives de dessin
		Graphics2D drawable = (Graphics2D) g;

		// On reccupere quelques infos provenant de la partie JComponent
		int width = getSize().width;
		int height = getSize().height;

		// On calcule le centre de la zone et un rayon
		if (center == null) {
			center = new Point(width/2, height/2);
		}

		// On efface tout
		drawable.clearRect(0, 0, width, height);

		// On affiche une petite image au milieu
		drawable.drawImage(img, center.x-20, center.y-20, 40, 40, null);
	}
}
