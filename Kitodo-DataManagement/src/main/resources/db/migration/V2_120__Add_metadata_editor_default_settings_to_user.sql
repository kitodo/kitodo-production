--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.
--

--
-- Migration: Add columns for default metadata editor settings to user table
--

-- Add columns 'show_pagination_by_default', 'show_comments_by_default' and 'default_gallery_view_mode' to 'user' table
ALTER TABLE user ADD show_pagination_by_default TINYINT(1) DEFAULT 0;
ALTER TABLE user ADD show_comments_by_default TINYINT(1) DEFAULT 0;
ALTER TABLE user ADD default_gallery_view_mode varchar(255) NOT NULL DEFAULT 'dataEditor.galleryStructuredView';
